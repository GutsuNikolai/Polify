import React, { useEffect, useMemo, useState } from "react";
import { ActivityIndicator, Alert, Button, ScrollView, StyleSheet, Text, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { AppStackParamList } from "../navigation/AppNavigator";
import { getSurvey } from "../api/surveys";
import { getActiveAttempt, listAttempts, startAttempt } from "../api/attempts";
import { normalizeError } from "../api/http";
import { SurveyDetailsResponse } from "../types/survey";
import { colors } from "../theme/colors";
import { glass } from "../theme/glass";

type Props = NativeStackScreenProps<AppStackParamList, "SurveyDetails">;

function baniToMdl(amountBani: number): string {
  const mdl = amountBani / 100;
  return `${mdl.toFixed(2)} MDL`;
}

function estimateMinutes(qCount: number): number {
  // MVP heuristic: ~25s per question.
  const minutes = Math.max(1, Math.ceil((qCount * 25) / 60));
  return minutes;
}

export function SurveyDetailsScreen({ route, navigation }: Props) {
  const { surveyId } = route.params;
  const [survey, setSurvey] = useState<SurveyDetailsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  const minutes = useMemo(() => (survey ? estimateMinutes(survey.questions.length) : 0), [survey]);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const data = await getSurvey(surveyId);
        setSurvey(data);
      } catch (e) {
        const ne = normalizeError(e);
        Alert.alert("Failed to load survey", ne.kind === "api" ? ne.apiError.message : ne.message);
      } finally {
        setLoading(false);
      }
    })();
  }, [surveyId]);

  const onStart = async () => {
    setBusy(true);
    try {
      // UX guard: if user already completed this survey, block immediately.
      const attempts = await listAttempts(surveyId);
      if (attempts.some((a) => a.status === "COMPLETED")) {
        Alert.alert("Already completed", "You already completed this survey.");
        return;
      }

      const active = await getActiveAttempt(surveyId);
      const attemptId = active?.attemptId ?? (await startAttempt(surveyId)).attemptId;
      navigation.navigate("Attempt", { surveyId, attemptId });
    } catch (e) {
      const ne = normalizeError(e);
      if (ne.kind === "api") {
        Alert.alert("Cannot start", `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}`);
      } else {
        Alert.alert("Cannot start", ne.message);
      }
    } finally {
      setBusy(false);
    }
  };

  if (loading || !survey) {
    return (
      <View style={styles.center}>
        <ActivityIndicator />
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.cover}>
        <View style={styles.coverSheen} pointerEvents="none" />
        <Text style={styles.coverTitle}>{survey.title}</Text>
        <View style={styles.metaRow}>
          <View style={styles.pill}>
            <Text style={styles.pillText}>{baniToMdl(survey.rewardAmountBani)}</Text>
          </View>
          <View style={styles.pill}>
            <Text style={styles.pillText}>~ {minutes} min</Text>
          </View>
          <View style={styles.pill}>
            <Text style={styles.pillText}>{survey.questions.length} questions</Text>
          </View>
        </View>
      </View>

      <Text style={styles.sectionTitle}>Description</Text>
      <Text style={styles.desc}>{survey.description}</Text>

      <View style={styles.actions}>
        <Button title={busy ? "Starting..." : "Start"} disabled={busy} onPress={onStart} />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg1 },
  content: { padding: 16, paddingBottom: 28 },
  center: { flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: colors.bg1 },
  cover: {
    ...glass.card,
    borderRadius: 18,
    padding: 16,
    overflow: "hidden",
  },
  coverSheen: {
    position: "absolute",
    top: -70,
    right: -70,
    width: 220,
    height: 220,
    borderRadius: 999,
    backgroundColor: colors.glassHighlight,
    opacity: 0.16,
  },
  coverTitle: { color: colors.text, fontSize: 22, fontWeight: "900" },
  metaRow: { flexDirection: "row", flexWrap: "wrap", gap: 10, marginTop: 14 },
  pill: {
    backgroundColor: colors.glassStrong,
    borderWidth: 1,
    borderColor: colors.glassBorder,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999,
  },
  pillText: { color: colors.text, fontWeight: "700" },
  sectionTitle: { marginTop: 18, color: colors.textDim, fontWeight: "800" },
  desc: { marginTop: 8, color: colors.textMuted, lineHeight: 20 },
  actions: { marginTop: 22 },
});
