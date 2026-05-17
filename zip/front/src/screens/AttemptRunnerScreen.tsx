import React, { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  Button,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { Picker } from "@react-native-picker/picker";
import { AppStackParamList } from "../navigation/AppNavigator";
import { completeAttempt, getAttempt, submitAnswer } from "../api/attempts";
import { getSurvey } from "../api/surveys";
import { normalizeError } from "../api/http";
import { AttemptDetailsResponse, SubmitAnswerRequest } from "../types/attempt";
import { SurveyDetailsResponse, SurveyQuestionDto, SurveyQuestionType } from "../types/survey";
import { QuestionShell } from "../components/QuestionShell";
import { OptionRow } from "../components/options/OptionRow";
import { getLastQuestionId, setLastQuestionId } from "../attempt/progressStorage";

type Props = NativeStackScreenProps<AppStackParamList, "Attempt">;

function getQuestionIndex(survey: SurveyDetailsResponse, questionId: number | null | undefined): number {
  if (!questionId) return 0;
  const idx = survey.questions.findIndex((q) => q.id === questionId);
  return idx >= 0 ? idx : 0;
}

function answerForQuestion(attempt: AttemptDetailsResponse, questionId: number) {
  return attempt.answers.find((a) => a.questionId === questionId) ?? null;
}

function canGoNext(
  q: SurveyQuestionDto,
  draft: DraftAnswer
): boolean {
  if (!q.required) return true;
  switch (q.type) {
    case "TEXT":
      return !!draft.textValue && draft.textValue.trim().length > 0;
    case "RADIO":
    case "SELECT":
      return typeof draft.optionId === "number";
    case "CHECKBOX":
      return Array.isArray(draft.optionIds) && draft.optionIds.length > 0;
    case "PRIORITY":
      // Required PRIORITY rule in backend: rank all options 1..N.
      return Array.isArray(draft.priorityOrder) && draft.priorityOrder.length === q.options.length;
    default:
      return false;
  }
}

type DraftAnswer = {
  textValue?: string;
  optionId?: number;
  optionIds?: number[];
  // For PRIORITY we store an ordered list of optionIds.
  priorityOrder?: number[];
};

function initDraft(q: SurveyQuestionDto, attempt: AttemptDetailsResponse): DraftAnswer {
  const existing = answerForQuestion(attempt, q.id);
  if (!existing) {
    if (q.type === "PRIORITY") {
      return { priorityOrder: q.options.map((o) => o.id) };
    }
    return {};
  }

  switch (q.type) {
    case "TEXT":
      return { textValue: existing.textValue ?? "" };
    case "RADIO":
    case "SELECT":
      return { optionId: existing.optionIds?.[0] };
    case "CHECKBOX":
      return { optionIds: existing.optionIds ?? [] };
    case "PRIORITY": {
      // If already answered, use stored ranks; else default to current option order.
      if (existing.priority && existing.priority.length > 0) {
        const byRank = [...existing.priority].sort((a, b) => a.rank - b.rank);
        return { priorityOrder: byRank.map((p) => p.optionId) };
      }
      return { priorityOrder: q.options.map((o) => o.id) };
    }
    default:
      return {};
  }
}

function toSubmitPayload(q: SurveyQuestionDto, draft: DraftAnswer): SubmitAnswerRequest {
  switch (q.type as SurveyQuestionType) {
    case "TEXT":
      return { questionId: q.id, textValue: (draft.textValue ?? "").trim() };
    case "RADIO":
    case "SELECT":
      return { questionId: q.id, optionId: draft.optionId ?? null };
    case "CHECKBOX":
      return { questionId: q.id, optionIds: draft.optionIds ?? [] };
    case "PRIORITY": {
      const order = draft.priorityOrder ?? [];
      const priority = order.map((optionId, idx) => ({ optionId, rank: idx + 1 }));
      return { questionId: q.id, priority };
    }
    default:
      return { questionId: q.id };
  }
}

export function AttemptRunnerScreen({ route, navigation }: Props) {
  const { surveyId, attemptId } = route.params;
  const [survey, setSurvey] = useState<SurveyDetailsResponse | null>(null);
  const [attempt, setAttempt] = useState<AttemptDetailsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  const [index, setIndex] = useState(0);
  const [draft, setDraft] = useState<DraftAnswer>({});

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const [s, a] = await Promise.all([getSurvey(surveyId), getAttempt(attemptId)]);
        const ordered = { ...s, questions: [...s.questions].sort((x, y) => x.position - y.position) };
        setSurvey(ordered);
        setAttempt(a);
        const last = await getLastQuestionId(attemptId);
        const startQid = last ?? a.nextQuestionId ?? ordered.questions[0]?.id ?? null;
        const idx = getQuestionIndex(ordered, startQid);
        setIndex(idx);
        setDraft(initDraft(ordered.questions[idx], a));
      } catch (e) {
        const ne = normalizeError(e);
        Alert.alert("Failed to load attempt", ne.kind === "api" ? ne.apiError.message : ne.message);
      } finally {
        setLoading(false);
      }
    })();
  }, [attemptId, surveyId]);

  const question = useMemo(() => {
    if (!survey) return null;
    return survey.questions[index] ?? null;
  }, [survey, index]);

  const progressLabel = useMemo(() => {
    if (!survey) return "";
    return `${index + 1} / ${survey.questions.length}`;
  }, [survey, index]);

  const syncDraftToQuestion = async (newIndex: number) => {
    if (!survey || !attempt) return;
    const q = survey.questions[newIndex];
    setIndex(newIndex);
    setDraft(initDraft(q, attempt));
    void setLastQuestionId(attempt.attemptId, q.id);
  };

  const onBack = async () => {
    if (!survey) return;
    if (index <= 0) return;
    await syncDraftToQuestion(index - 1);
  };

  const onNext = async () => {
    if (!survey || !attempt || !question) return;

    // Local UX guard (backend also enforces sequential required).
    if (!canGoNext(question, draft)) {
      Alert.alert("Required", "Please answer the required question before continuing.");
      return;
    }

    setBusy(true);
    try {
      const payload = toSubmitPayload(question, draft);
      await submitAnswer(attempt.attemptId, payload);
      const updated = await getAttempt(attempt.attemptId);
      setAttempt(updated);

      const nextIdx = index + 1 < survey.questions.length ? index + 1 : index;
      if (nextIdx !== index) {
        const nextQ = survey.questions[nextIdx];
        setIndex(nextIdx);
        setDraft(initDraft(nextQ, updated));
        void setLastQuestionId(updated.attemptId, nextQ.id);
      } else {
        // Last question: stay on it (no popup).
        void setLastQuestionId(updated.attemptId, question.id);
      }
    } catch (e) {
      const ne = normalizeError(e);
      if (ne.kind === "api") {
        Alert.alert("Cannot save", `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}`);
      } else {
        Alert.alert("Cannot save", ne.message);
      }
    } finally {
      setBusy(false);
    }
  };

  const onComplete = async () => {
    if (!survey || !attempt || !question) return;

    // Save last question answer first (same as Next).
    if (!canGoNext(question, draft)) {
      Alert.alert("Required", "Please answer the required question before continuing.");
      return;
    }

    setBusy(true);
    try {
      const payload = toSubmitPayload(question, draft);
      await submitAnswer(attempt.attemptId, payload);
      await completeAttempt(attempt.attemptId);
      void setLastQuestionId(attempt.attemptId, question.id);
      navigation.replace("AttemptCompleted", { surveyId, attemptId: attempt.attemptId });
    } catch (e) {
      const ne = normalizeError(e);
      if (ne.kind === "api") {
        Alert.alert("Cannot complete", `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}`);
      } else {
        Alert.alert("Cannot complete", ne.message);
      }
    } finally {
      setBusy(false);
    }
  };

  if (loading || !survey || !attempt || !question) {
    return (
      <View style={styles.center}>
        <ActivityIndicator />
      </View>
    );
  }

  const isLast = index + 1 === survey.questions.length;
  const footer = (
    <View style={styles.footerRow}>
      <View style={{ flex: 1 }}>
        <Button title="Back" disabled={busy || index === 0} onPress={onBack} />
      </View>
      <View style={{ width: 12 }} />
      <View style={{ flex: 1 }}>
        <Button
          title={busy ? (isLast ? "Completing..." : "Saving...") : (isLast ? "Complete" : "Next")}
          disabled={busy}
          onPress={isLast ? onComplete : onNext}
        />
      </View>
    </View>
  );

  return (
    <QuestionShell
      title={question.text}
      progressLabel={progressLabel}
      onExit={() => {
        void setLastQuestionId(attempt.attemptId, question.id);
        navigation.popToTop();
      }}
      footer={footer}
    >
      {question.required ? <Text style={styles.required}>Required</Text> : <Text style={styles.optional}>Optional</Text>}
      <View style={{ height: 10 }} />

      {question.type === "TEXT" ? (
        <TextInput
          style={styles.textInput}
          placeholder="Type your answer..."
          placeholderTextColor="#64748B"
          multiline
          maxLength={500}
          value={draft.textValue ?? ""}
          onChangeText={(t) => setDraft((d) => ({ ...d, textValue: t }))}
        />
      ) : null}

      {question.type === "RADIO" || question.type === "SELECT" ? (
        <View>
          {question.type === "RADIO" ? (
            question.options
              .slice()
              .sort((a, b) => a.position - b.position)
              .map((o) => (
                <OptionRow
                  key={o.id}
                  label={o.label}
                  selected={draft.optionId === o.id}
                  onPress={() => setDraft((d) => ({ ...d, optionId: o.id }))}
                  variant="radio"
                />
              ))
          ) : (
            <View style={styles.selectBox}>
              <Picker
                selectedValue={draft.optionId ?? null}
                onValueChange={(v) => setDraft((d) => ({ ...d, optionId: typeof v === "number" ? v : Number(v) }))}
                dropdownIconColor="#E2E8F0"
                style={styles.picker}
              >
                <Picker.Item label="Select an option..." value={null as any} />
                {question.options
                  .slice()
                  .sort((a, b) => a.position - b.position)
                  .map((o) => (
                    <Picker.Item key={o.id} label={o.label} value={o.id} />
                  ))}
              </Picker>
            </View>
          )}
        </View>
      ) : null}

      {question.type === "CHECKBOX" ? (
        <View>
          {question.options
            .slice()
            .sort((a, b) => a.position - b.position)
            .map((o) => {
              const selected = (draft.optionIds ?? []).includes(o.id);
              return (
                <OptionRow
                  key={o.id}
                  label={o.label}
                  selected={selected}
                  onPress={() => {
                    setDraft((d) => {
                      const prev = d.optionIds ?? [];
                      if (prev.includes(o.id)) return { ...d, optionIds: prev.filter((x) => x !== o.id) };
                      return { ...d, optionIds: [...prev, o.id] };
                    });
                  }}
                  variant="checkbox"
                />
              );
            })}
        </View>
      ) : null}

      {question.type === "PRIORITY" ? (
        <View>
          <Text style={styles.hint}>Rank all options (1 = highest).</Text>
          <View style={{ height: 10 }} />
          {(draft.priorityOrder ?? question.options.map((o) => o.id)).map((optionId, idx) => {
            const opt = question.options.find((o) => o.id === optionId);
            if (!opt) return null;
            const canUp = idx > 0;
            const canDown = idx < (draft.priorityOrder?.length ?? 0) - 1;
            return (
              <OptionRow
                key={opt.id}
                label={`${idx + 1}. ${opt.label}`}
                selected={true}
                onPress={() => {}}
                right={
                  <View style={{ flexDirection: "row", gap: 8 }}>
                    <Text
                      style={[styles.rankBtn, !canUp && styles.rankBtnDisabled]}
                      onPress={() => {
                        if (!canUp) return;
                        setDraft((d) => {
                          const order = [...(d.priorityOrder ?? [])];
                          const tmp = order[idx - 1];
                          order[idx - 1] = order[idx];
                          order[idx] = tmp;
                          return { ...d, priorityOrder: order };
                        });
                      }}
                    >
                      Up
                    </Text>
                    <Text
                      style={[styles.rankBtn, !canDown && styles.rankBtnDisabled]}
                      onPress={() => {
                        if (!canDown) return;
                        setDraft((d) => {
                          const order = [...(d.priorityOrder ?? [])];
                          const tmp = order[idx + 1];
                          order[idx + 1] = order[idx];
                          order[idx] = tmp;
                          return { ...d, priorityOrder: order };
                        });
                      }}
                    >
                      Down
                    </Text>
                  </View>
                }
              />
            );
          })}
        </View>
      ) : null}
    </QuestionShell>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: "#0B1220" },
  footerRow: { flexDirection: "row" },
  required: { color: "#FCA5A5", fontWeight: "800" },
  optional: { color: "#94A3B8", fontWeight: "800" },
  hint: { color: "#A5B4FC", fontWeight: "700" },
  selectBox: {
    backgroundColor: "#0F172A",
    borderWidth: 1,
    borderColor: "#1F2A44",
    borderRadius: 14,
    overflow: "hidden",
  },
  picker: {
    color: "#E2E8F0",
  },
  textInput: {
    minHeight: 120,
    backgroundColor: "#0F172A",
    borderWidth: 1,
    borderColor: "#1F2A44",
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
    color: "#E2E8F0",
    textAlignVertical: "top",
  },
  rankBtn: {
    color: "#93C5FD",
    fontWeight: "800",
  },
  rankBtnDisabled: {
    color: "#475569",
  },
});
