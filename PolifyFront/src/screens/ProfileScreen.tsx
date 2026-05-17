import React, { useEffect, useMemo, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  Button,
  Image,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { Picker } from "@react-native-picker/picker";
import { getProfile, updateProfile } from "../api/profile";
import { normalizeError } from "../api/http";
import { UpdateUserProfileRequest, UserProfileResponse } from "../types/profile";
import { useAuthStore } from "../store/authStore";
import { useSafeAreaInsets } from "react-native-safe-area-context";

function toDraft(p: UserProfileResponse): UpdateUserProfileRequest {
  return {
    email: p.email ?? null,
    fullName: p.fullName ?? null,
    gender: p.gender ?? null,
    birthDate: p.birthDate ?? null,
    country: p.country ?? null,
    city: p.city ?? null,
  };
}

export function ProfileScreen() {
  const insets = useSafeAreaInsets();
  const signOut = useAuthStore((s) => s.signOut);

  const [profile, setProfile] = useState<UserProfileResponse | null>(null);
  const [draft, setDraft] = useState<UpdateUserProfileRequest>({});
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  const headerPad = useMemo(() => Math.max(16, 16 + insets.top), [insets.top]);

  const load = async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const p = await getProfile();
      setProfile(p);
      setDraft(toDraft(p));
    } catch (e) {
      const ne = normalizeError(e);
      const msg = ne.kind === "api" ? ne.apiError.message : ne.message;
      setLoadError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const onSave = async () => {
    setBusy(true);
    try {
      const updated = await updateProfile(draft);
      setProfile(updated);
      setDraft(toDraft(updated));
      Alert.alert("Saved", "Profile updated.");
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

  if (loading) {
    return (
      <View style={[styles.center, { paddingTop: headerPad }]}>
        <ActivityIndicator />
      </View>
    );
  }

  if (!profile) {
    return (
      <View style={[styles.center, { paddingTop: headerPad, paddingHorizontal: 24 }]}>
        <Text style={{ color: "#F8FAFC", fontSize: 18, fontWeight: "800", textAlign: "center" }}>
          Failed to load profile
        </Text>
        <Text style={{ marginTop: 10, color: "#A5B4FC", textAlign: "center" }}>
          {loadError ?? "Unknown error"}
        </Text>
        <View style={{ height: 18 }} />
        <Button title="Retry" onPress={() => void load()} />
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={[styles.content, { paddingTop: headerPad }]}>
      <View style={styles.headerRow}>
        <View style={{ flex: 1 }}>
          <Text style={styles.title}>Profile</Text>
          <Text style={styles.subtitle}>Edit your public info. Sensitive data is never logged.</Text>
        </View>
        <Text style={styles.signOut} onPress={() => signOut()}>
          Sign out
        </Text>
      </View>

      <View style={styles.avatarRow}>
        <Image
          source={{
            uri: "https://i.pravatar.cc/256?img=12",
          }}
          style={styles.avatar}
        />
        <View style={{ flex: 1 }}>
          <Text style={styles.meta}>login: {profile.login}</Text>
          <Text style={styles.meta}>phone: {profile.phoneNumber}</Text>
          <Text style={styles.meta}>verified: {profile.verified ? "yes" : "no"}</Text>
        </View>
      </View>

      <Text style={styles.sectionTitle}>Details</Text>

      <Text style={styles.label}>Full name</Text>
      <TextInput
        style={styles.input}
        placeholder="John Doe"
        placeholderTextColor="#64748B"
        value={draft.fullName ?? ""}
        onChangeText={(t) => setDraft((d) => ({ ...d, fullName: t }))}
      />

      <Text style={styles.label}>Email</Text>
      <TextInput
        style={styles.input}
        placeholder="email@example.com"
        placeholderTextColor="#64748B"
        autoCapitalize="none"
        keyboardType="email-address"
        value={draft.email ?? ""}
        onChangeText={(t) => setDraft((d) => ({ ...d, email: t }))}
      />

      <Text style={styles.label}>Gender</Text>
      <View style={styles.selectBox}>
        <Picker
          selectedValue={draft.gender ?? null}
          onValueChange={(v) => setDraft((d) => ({ ...d, gender: (v as any) ?? null }))}
          dropdownIconColor="#E2E8F0"
          style={styles.picker}
        >
          <Picker.Item label="Not set" value={null as any} />
          <Picker.Item label="Male" value="MALE" />
          <Picker.Item label="Female" value="FEMALE" />
        </Picker>
      </View>

      <Text style={styles.label}>Birth date (YYYY-MM-DD)</Text>
      <TextInput
        style={styles.input}
        placeholder="1999-12-31"
        placeholderTextColor="#64748B"
        autoCapitalize="none"
        value={draft.birthDate ?? ""}
        onChangeText={(t) => setDraft((d) => ({ ...d, birthDate: t }))}
      />

      <Text style={styles.label}>Country</Text>
      <TextInput
        style={styles.input}
        placeholder="MD"
        placeholderTextColor="#64748B"
        autoCapitalize="characters"
        value={draft.country ?? ""}
        onChangeText={(t) => setDraft((d) => ({ ...d, country: t }))}
      />

      <Text style={styles.label}>City</Text>
      <TextInput
        style={styles.input}
        placeholder="Chisinau"
        placeholderTextColor="#64748B"
        value={draft.city ?? ""}
        onChangeText={(t) => setDraft((d) => ({ ...d, city: t }))}
      />

      <View style={styles.actions}>
        <Button title={busy ? "Saving..." : "Save"} disabled={busy} onPress={onSave} />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#0B1220" },
  content: { padding: 16, paddingBottom: 28 },
  center: { flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: "#0B1220" },
  headerRow: { flexDirection: "row", alignItems: "center", gap: 12, marginBottom: 14 },
  title: { fontSize: 28, fontWeight: "800", color: "#F8FAFC" },
  subtitle: { marginTop: 6, color: "#A5B4FC" },
  signOut: { color: "#93C5FD", fontWeight: "700" },
  avatarRow: {
    flexDirection: "row",
    gap: 14,
    backgroundColor: "#0F172A",
    borderRadius: 18,
    borderWidth: 1,
    borderColor: "#1F2A44",
    padding: 16,
  },
  avatar: { width: 64, height: 64, borderRadius: 18, backgroundColor: "#111A2E" },
  meta: { color: "#E2E8F0", marginTop: 2 },
  sectionTitle: { marginTop: 18, color: "#A5B4FC", fontWeight: "800" },
  label: { marginTop: 14, marginBottom: 6, color: "#CBD5E1", fontWeight: "700" },
  input: {
    backgroundColor: "#0F172A",
    color: "#E2E8F0",
    borderWidth: 1,
    borderColor: "#1F2A44",
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  selectBox: {
    backgroundColor: "#0F172A",
    borderWidth: 1,
    borderColor: "#1F2A44",
    borderRadius: 14,
    overflow: "hidden",
  },
  picker: { color: "#E2E8F0" },
  actions: { marginTop: 20 },
});
