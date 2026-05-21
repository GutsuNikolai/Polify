import React, { useCallback, useEffect, useState } from "react";
import { ActivityIndicator, Alert, RefreshControl, ScrollView, StyleSheet, Text, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { listSurveys } from "../api/surveys";
import { normalizeError } from "../api/http";
import { SurveyListItem } from "../types/survey";
import { SurveyCard } from "../components/SurveyCard";
import { AppStackParamList } from "../navigation/AppNavigator";
import { useAuthStore } from "../store/authStore";
import { colors } from "../theme/colors";

type Props = NativeStackScreenProps<AppStackParamList, "Surveys">;

export function SurveysListScreen({ navigation }: Props) {
  const signOut = useAuthStore((s) => s.signOut);
  const [items, setItems] = useState<SurveyListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await listSurveys();
      setItems(data);
    } catch (e) {
      const ne = normalizeError(e);
      if (ne.kind === "api") {
        Alert.alert("Failed to load surveys", `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}`);
      } else {
        Alert.alert("Failed to load surveys", ne.message);
      }
    }
  }, []);

  useEffect(() => {
    (async () => {
      setLoading(true);
      await load();
      setLoading(false);
    })();
  }, [load]);

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    await load();
    setRefreshing(false);
  }, [load]);

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator />
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.accent2} />}
    >
      <View style={styles.header}>
        <View style={{ flex: 1 }}>
          <Text style={styles.title}>Surveys</Text>
          <Text style={styles.subtitle}>Pick one and earn rewards.</Text>
        </View>
        <Text style={styles.signOut} onPress={() => signOut()}>
          Sign out
        </Text>
      </View>

      {items.map((s) => (
        <SurveyCard key={s.id} survey={s} onPress={() => navigation.navigate("SurveyDetails", { surveyId: s.id })} />
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg1 },
  content: { padding: 16, paddingBottom: 28 },
  center: { flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: colors.bg1 },
  header: { flexDirection: "row", alignItems: "center", gap: 12, marginBottom: 14 },
  title: { fontSize: 28, fontWeight: "800", color: colors.text },
  subtitle: { marginTop: 6, color: colors.textDim },
  signOut: { color: colors.accent2, fontWeight: "700" },
});
