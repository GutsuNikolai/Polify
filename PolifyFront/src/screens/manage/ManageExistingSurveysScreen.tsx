import React, { useCallback, useEffect, useMemo, useState } from "react";
import { ActivityIndicator, Alert, Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { archiveSurvey, listAllSurveysForManage } from "../../api/surveysAdmin";
import { normalizeError } from "../../api/http";
import { ManageSurveyListItem } from "../../types/surveyAdmin";
import { colors } from "../../theme/colors";
import { glass } from "../../theme/glass";

import type { ManageStackParamList } from "./ManageSurveysScreen";

type Props = NativeStackScreenProps<ManageStackParamList, "ManageExisting">;

function fmtArchivedAt(v?: string | null): string | null {
  if (!v) return null;
  // Keep it simple for MVP.
  return v.replace("T", " ").replace("Z", " UTC");
}

export function ManageExistingSurveysScreen({ navigation }: Props) {
  const insets = useSafeAreaInsets();
  const topPad = useMemo(() => Math.max(16, 16 + insets.top), [insets.top]);

  const [items, setItems] = useState<ManageSurveyListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [busyId, setBusyId] = useState<number | null>(null);

  const load = useCallback(async () => {
    const data = await listAllSurveysForManage();
    setItems(data);
  }, []);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        await load();
      } catch (e) {
        const ne = normalizeError(e);
        Alert.alert("Failed to load", ne.kind === "api" ? `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}` : ne.message);
      } finally {
        setLoading(false);
      }
    })();
  }, [load]);

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    try {
      await load();
    } finally {
      setRefreshing(false);
    }
  }, [load]);

  const onArchive = async (id: number) => {
    setBusyId(id);
    try {
      await archiveSurvey(id);
      await load();
    } catch (e) {
      const ne = normalizeError(e);
      Alert.alert("Cannot archive", ne.kind === "api" ? `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}` : ne.message);
    } finally {
      setBusyId(null);
    }
  };

  if (loading) {
    return (
      <View style={[styles.center, { paddingTop: topPad }]}>
        <ActivityIndicator />
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={[styles.content, { paddingTop: topPad }]}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.accent2} />}
    >
      <View style={styles.headerRow}>
        <View style={{ flex: 1 }}>
          <Text style={styles.title}>Existing surveys</Text>
          <Text style={styles.subtitle}>Archive surveys to hide them from users.</Text>
        </View>
        <Pressable onPress={() => navigation.goBack()} style={({ pressed }) => [styles.backBtn, pressed && { opacity: 0.85 }]}>
          <Text style={styles.backText}>Back</Text>
        </Pressable>
      </View>

      {items.map((s) => {
        const archivedAt = fmtArchivedAt(s.archivedAt ?? null);
        const canArchive = !s.archived && busyId !== s.id;
        const isBusy = busyId === s.id;
        return (
          <View key={s.id} style={styles.card}>
            <View style={styles.cardSheen} pointerEvents="none" />
            <View style={{ flex: 1 }}>
              <Text style={styles.cardTitle} numberOfLines={2}>
                {s.title}
              </Text>
              <Text style={styles.cardMeta}>
                {s.archived ? `ARCHIVED${archivedAt ? ` • ${archivedAt}` : ""}` : "ACTIVE"}
              </Text>
            </View>

            <Pressable
              disabled={!canArchive}
              onPress={() => void onArchive(s.id)}
              style={({ pressed }) => [
                styles.archiveBtn,
                s.archived && styles.archiveBtnDisabled,
                isBusy && styles.archiveBtnDisabled,
                pressed && canArchive && { opacity: 0.9 },
              ]}
            >
              <Text style={styles.archiveText}>{s.archived ? "Archived" : isBusy ? "..." : "Archive"}</Text>
            </Pressable>
          </View>
        );
      })}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg1 },
  content: { padding: 16, paddingBottom: 28, gap: 12 },
  center: { flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: colors.bg1 },
  headerRow: { flexDirection: "row", alignItems: "center", gap: 12, marginBottom: 6 },
  title: { fontSize: 24, fontWeight: "900", color: colors.text },
  subtitle: { marginTop: 6, color: colors.textDim },
  backBtn: { ...glass.card, borderRadius: 12, paddingHorizontal: 14, paddingVertical: 10 },
  backText: { color: colors.text, fontWeight: "800" },
  card: {
    ...glass.card,
    borderRadius: 18,
    padding: 14,
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    overflow: "hidden",
  },
  cardSheen: {
    position: "absolute",
    top: -70,
    right: -70,
    width: 220,
    height: 220,
    borderRadius: 999,
    backgroundColor: colors.glassHighlight,
    opacity: 0.12,
  },
  cardTitle: { color: colors.text, fontWeight: "900", fontSize: 16 },
  cardMeta: { marginTop: 6, color: colors.textDim, fontWeight: "800", fontSize: 12 },
  archiveBtn: {
    backgroundColor: "rgba(90, 169, 255, 0.85)",
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: colors.glassBorder,
  },
  archiveBtnDisabled: {
    backgroundColor: "rgba(120, 150, 190, 0.22)",
  },
  archiveText: { color: colors.text, fontWeight: "900" },
});

