import React, { useEffect, useMemo, useState } from "react";
import { ActivityIndicator, Alert, Button, StyleSheet, Text, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { normalizeError } from "../../api/http";
import { getProfile } from "../../api/profile";
import { Role, UserProfileResponse } from "../../types/profile";
import { useSafeAreaInsets } from "react-native-safe-area-context";

export type ManageStackParamList = {
  ManageHome: undefined;
  CreateSurvey: undefined;
  ManageExisting: undefined;
};

type Props = NativeStackScreenProps<ManageStackParamList, "ManageHome">;

function isModerator(role: Role): boolean {
  return role === "MODERATOR" || role === "ADMIN";
}

export function ManageSurveysScreen({ navigation }: Props) {
  const insets = useSafeAreaInsets();
  const topPad = useMemo(() => Math.max(16, 16 + insets.top), [insets.top]);

  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const p = await getProfile();
        setProfile(p);
      } catch (e) {
        const ne = normalizeError(e);
        Alert.alert("Failed to load", ne.kind === "api" ? ne.apiError.message : ne.message);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return (
      <View style={[styles.center, { paddingTop: topPad }]}>
        <ActivityIndicator />
      </View>
    );
  }

  const role = profile?.role ?? "USER";
  if (!isModerator(role)) {
    return (
      <View style={[styles.center, { paddingTop: topPad, paddingHorizontal: 24 }]}>
        <Text style={styles.title}>Manage surveys</Text>
        <Text style={styles.subtitle}>You are not a moderator.</Text>
      </View>
    );
  }

  return (
    <View style={[styles.container, { paddingTop: topPad }]}>
      <View style={styles.header}>
        <Text style={styles.title}>Manage surveys</Text>
        <Text style={styles.subtitle}>Moderator tools (MVP).</Text>
      </View>

      <View style={{ height: 10 }} />

      <Button title="Create survey" onPress={() => navigation.navigate("CreateSurvey")} />

      <View style={{ height: 10 }} />

      <Button title="Manage existing surveys" onPress={() => navigation.navigate("ManageExisting")} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#0B1220", padding: 16 },
  center: { flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: "#0B1220" },
  header: { marginBottom: 10 },
  title: { fontSize: 24, fontWeight: "900", color: "#F8FAFC" },
  subtitle: { marginTop: 6, color: "#A5B4FC" },
});
