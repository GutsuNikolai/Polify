import React, { useMemo, useState } from "react";
import {
  Alert,
  Button,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { Picker } from "@react-native-picker/picker";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { createSurvey } from "../../api/surveys";
import { normalizeError } from "../../api/http";
import { CreateSurveyRequest, CreateSurveyQuestion } from "../../types/surveyAdmin";
import { SurveyQuestionType } from "../../types/survey";

import type { ManageStackParamList } from "./ManageSurveysScreen";

type Props = NativeStackScreenProps<ManageStackParamList, "CreateSurvey">;

type DraftOption = { label: string; value: string };
type DraftQuestion = {
  type: SurveyQuestionType;
  text: string;
  required: boolean;
  options: DraftOption[];
};

function defaultQuestion(type: SurveyQuestionType): DraftQuestion {
  return {
    type,
    text: "",
    required: true,
    options: type === "TEXT" ? [] : [{ label: "", value: "" }],
  };
}

function requiresOptions(type: SurveyQuestionType): boolean {
  return type !== "TEXT";
}

export function CreateSurveyScreen({ navigation }: Props) {
  const insets = useSafeAreaInsets();
  const topPad = useMemo(() => Math.max(16, 16 + insets.top), [insets.top]);

  const [busy, setBusy] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [rewardAmountBani, setRewardAmountBani] = useState("500");
  const [targetCompletions, setTargetCompletions] = useState("100");
  const [questions, setQuestions] = useState<DraftQuestion[]>([
    defaultQuestion("TEXT"),
  ]);

  const onAddQuestion = () => setQuestions((qs) => [...qs, defaultQuestion("TEXT")]);
  const onRemoveQuestion = (idx: number) => setQuestions((qs) => qs.filter((_, i) => i !== idx));

  const toRequest = (): CreateSurveyRequest => {
    const reward = Number(rewardAmountBani);
    const target = Number(targetCompletions);
    const reqQuestions: CreateSurveyQuestion[] = questions.map((q, idx) => {
      const position = idx + 1;
      if (!requiresOptions(q.type)) {
        return {
          type: q.type,
          text: q.text,
          position,
          required: q.required,
        };
      }
      return {
        type: q.type,
        text: q.text,
        position,
        required: q.required,
        options: q.options.map((o, oi) => ({
          label: o.label,
          value: o.value,
          position: oi + 1,
        })),
      };
    });

    return {
      title,
      description: description.trim() ? description : null,
      rewardAmountBani: Number.isFinite(reward) ? reward : 0,
      targetCompletions: Number.isFinite(target) ? target : 1,
      questions: reqQuestions,
    };
  };

  const validateDraft = (): string | null => {
    if (!title.trim()) return "Title is required";
    if (questions.length === 0) return "At least one question is required";
    const reward = Number(rewardAmountBani);
    if (!Number.isFinite(reward) || reward < 0 || reward > 9900) return "rewardAmountBani must be 0..9900";
    const target = Number(targetCompletions);
    if (!Number.isFinite(target) || target < 1) return "targetCompletions must be >= 1";

    for (let i = 0; i < questions.length; i++) {
      const q = questions[i];
      if (!q.text.trim()) return `Question ${i + 1}: text is required`;
      if (requiresOptions(q.type)) {
        if (q.options.length === 0) return `Question ${i + 1}: options are required`;
        const values = new Set<string>();
        for (let oi = 0; oi < q.options.length; oi++) {
          const o = q.options[oi];
          if (!o.label.trim()) return `Question ${i + 1}, option ${oi + 1}: label required`;
          if (!o.value.trim()) return `Question ${i + 1}, option ${oi + 1}: value required`;
          const v = o.value.trim();
          if (values.has(v)) return `Question ${i + 1}: duplicate option value '${v}'`;
          values.add(v);
        }
      }
    }
    return null;
  };

  const onSubmit = async () => {
    const err = validateDraft();
    if (err) {
      Alert.alert("Invalid", err);
      return;
    }

    setBusy(true);
    try {
      const resp = await createSurvey(toRequest());
      Alert.alert("Created", `surveyId: ${resp.surveyId}`);
      navigation.goBack();
    } catch (e) {
      const ne = normalizeError(e);
      if (ne.kind === "api") {
        Alert.alert("Cannot create", `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}`);
      } else {
        Alert.alert("Cannot create", ne.message);
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={[styles.content, { paddingTop: topPad }]}>
      <Text style={styles.title}>Create survey</Text>
      <Text style={styles.subtitle}>Moderator-only. Surveys are immutable after creation in MVP.</Text>

      <Text style={styles.label}>Title</Text>
      <TextInput
        style={styles.input}
        placeholder="Survey title"
        placeholderTextColor="#64748B"
        value={title}
        onChangeText={setTitle}
      />

      <Text style={styles.label}>Description</Text>
      <TextInput
        style={[styles.input, { minHeight: 80 }]}
        placeholder="Description"
        placeholderTextColor="#64748B"
        multiline
        value={description}
        onChangeText={setDescription}
      />

      <View style={styles.row2}>
        <View style={{ flex: 1 }}>
          <Text style={styles.label}>Reward (bani)</Text>
          <TextInput
            style={styles.input}
            keyboardType="number-pad"
            value={rewardAmountBani}
            onChangeText={setRewardAmountBani}
          />
        </View>
        <View style={{ width: 12 }} />
        <View style={{ flex: 1 }}>
          <Text style={styles.label}>Target completions</Text>
          <TextInput
            style={styles.input}
            keyboardType="number-pad"
            value={targetCompletions}
            onChangeText={setTargetCompletions}
          />
        </View>
      </View>

      <Text style={styles.section}>Questions</Text>
      {questions.map((q, idx) => (
        <View key={idx} style={styles.qCard}>
          <View style={styles.qHeaderRow}>
            <Text style={styles.qTitle}>Question {idx + 1}</Text>
            {questions.length > 1 ? (
              <Text style={styles.remove} onPress={() => onRemoveQuestion(idx)}>
                Remove
              </Text>
            ) : null}
          </View>

          <Text style={styles.smallLabel}>Type</Text>
          <View style={styles.selectBox}>
            <Picker
              selectedValue={q.type}
              onValueChange={(v) =>
                setQuestions((qs) =>
                  qs.map((qq, i) =>
                    i === idx
                      ? {
                          ...qq,
                          type: v as SurveyQuestionType,
                          options: requiresOptions(v as SurveyQuestionType) ? (qq.options.length ? qq.options : [{ label: "", value: "" }]) : [],
                        }
                      : qq
                  )
                )
              }
              dropdownIconColor="#E2E8F0"
              style={styles.picker}
            >
              <Picker.Item label="TEXT" value="TEXT" />
              <Picker.Item label="RADIO" value="RADIO" />
              <Picker.Item label="CHECKBOX" value="CHECKBOX" />
              <Picker.Item label="SELECT" value="SELECT" />
              <Picker.Item label="PRIORITY" value="PRIORITY" />
            </Picker>
          </View>

          <View style={styles.rowSwitch}>
            <Text style={styles.smallLabel}>Required</Text>
            <Switch
              value={q.required}
              onValueChange={(v) =>
                setQuestions((qs) => qs.map((qq, i) => (i === idx ? { ...qq, required: v } : qq)))
              }
            />
          </View>

          <Text style={styles.smallLabel}>Text</Text>
          <TextInput
            style={styles.input}
            placeholder="Question text"
            placeholderTextColor="#64748B"
            value={q.text}
            onChangeText={(t) => setQuestions((qs) => qs.map((qq, i) => (i === idx ? { ...qq, text: t } : qq)))}
          />

          {requiresOptions(q.type) ? (
            <View style={{ marginTop: 12 }}>
              <View style={styles.qHeaderRow}>
                <Text style={styles.smallLabel}>Options</Text>
                <Text
                  style={styles.add}
                  onPress={() =>
                    setQuestions((qs) =>
                      qs.map((qq, i) => (i === idx ? { ...qq, options: [...qq.options, { label: "", value: "" }] } : qq))
                    )
                  }
                >
                  Add option
                </Text>
              </View>

              {q.options.map((o, oi) => (
                <View key={oi} style={styles.optRow}>
                  <TextInput
                    style={[styles.input, { flex: 1 }]}
                    placeholder={`Label ${oi + 1}`}
                    placeholderTextColor="#64748B"
                    value={o.label}
                    onChangeText={(t) =>
                      setQuestions((qs) =>
                        qs.map((qq, i) =>
                          i === idx
                            ? { ...qq, options: qq.options.map((oo, j) => (j === oi ? { ...oo, label: t } : oo)) }
                            : qq
                        )
                      )
                    }
                  />
                  <View style={{ width: 10 }} />
                  <TextInput
                    style={[styles.input, { flex: 1 }]}
                    placeholder={`Value ${oi + 1}`}
                    placeholderTextColor="#64748B"
                    autoCapitalize="characters"
                    value={o.value}
                    onChangeText={(t) =>
                      setQuestions((qs) =>
                        qs.map((qq, i) =>
                          i === idx
                            ? { ...qq, options: qq.options.map((oo, j) => (j === oi ? { ...oo, value: t } : oo)) }
                            : qq
                        )
                      )
                    }
                  />
                  <View style={{ width: 10 }} />
                  {q.options.length > 1 ? (
                    <Text
                      style={styles.removeOpt}
                      onPress={() =>
                        setQuestions((qs) =>
                          qs.map((qq, i) =>
                            i === idx ? { ...qq, options: qq.options.filter((_, j) => j !== oi) } : qq
                          )
                        )
                      }
                    >
                      X
                    </Text>
                  ) : null}
                </View>
              ))}
            </View>
          ) : null}
        </View>
      ))}

      <View style={{ height: 8 }} />
      <Button title="Add question" onPress={onAddQuestion} />
      <View style={{ height: 12 }} />
      <Button title={busy ? "Creating..." : "Create"} disabled={busy} onPress={onSubmit} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#0B1220" },
  content: { padding: 16, paddingBottom: 40 },
  title: { fontSize: 24, fontWeight: "900", color: "#F8FAFC" },
  subtitle: { marginTop: 8, color: "#A5B4FC", lineHeight: 20 },
  section: { marginTop: 18, color: "#A5B4FC", fontWeight: "900" },
  label: { marginTop: 14, marginBottom: 6, color: "#CBD5E1", fontWeight: "800" },
  smallLabel: { color: "#CBD5E1", fontWeight: "800" },
  input: {
    backgroundColor: "#0F172A",
    color: "#E2E8F0",
    borderWidth: 1,
    borderColor: "#1F2A44",
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  row2: { flexDirection: "row", alignItems: "center", marginTop: 6 },
  qCard: {
    marginTop: 14,
    backgroundColor: "#0F172A",
    borderWidth: 1,
    borderColor: "#1F2A44",
    borderRadius: 18,
    padding: 14,
  },
  qHeaderRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
  qTitle: { color: "#F8FAFC", fontWeight: "900" },
  remove: { color: "#FCA5A5", fontWeight: "900" },
  add: { color: "#93C5FD", fontWeight: "900" },
  rowSwitch: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginTop: 12 },
  selectBox: {
    marginTop: 8,
    backgroundColor: "#0B1220",
    borderWidth: 1,
    borderColor: "#1F2A44",
    borderRadius: 14,
    overflow: "hidden",
  },
  picker: { color: "#E2E8F0" },
  optRow: { flexDirection: "row", alignItems: "center", marginTop: 10 },
  removeOpt: { color: "#FCA5A5", fontWeight: "900", paddingHorizontal: 8 },
});

