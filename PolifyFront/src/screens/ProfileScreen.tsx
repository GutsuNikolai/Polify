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
import { listLedgerEntries } from "../api/ledger";
import { normalizeError } from "../api/http";
import { UpdateUserProfileRequest, UserProfileResponse } from "../types/profile";
import { useAuthStore } from "../store/authStore";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors } from "../theme/colors";
import { glass } from "../theme/glass";

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
  const [balanceBani, setBalanceBani] = useState<number | null>(null);

  const headerPad = useMemo(() => Math.max(16, 16 + insets.top), [insets.top]);

  const balanceLabel = useMemo(() => {
    const bani = balanceBani ?? 0;
    const mdl = bani / 100;
    return `${mdl.toFixed(2)} MDL`;
  }, [balanceBani]);

  const load = async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [p, ledger] = await Promise.all([getProfile(), listLedgerEntries()]);
      setProfile(p);
      setDraft(toDraft(p));
      // MVP: treat CREATED/CONFIRMED as "earned", ignore FAILED.
      const sum = ledger.filter((e) => e.status !== "FAILED").reduce((acc, e) => acc + (e.amountBani ?? 0), 0);
      setBalanceBani(sum);
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
        <View style={styles.headerRight}>
          <View style={styles.balancePill}>
            <Text style={styles.balanceLabel}>Balance</Text>
            <Text style={styles.balanceValue}>{balanceLabel}</Text>
          </View>
          <Text style={styles.signOut} onPress={() => signOut()}>
            Sign out
          </Text>
        </View>
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
  container: { flex: 1, backgroundColor: colors.bg1 },
  content: { padding: 16, paddingBottom: 28 },
  center: { flex: 1, alignItems: "center", justifyContent: "center", backgroundColor: colors.bg1 },
  headerRow: { flexDirection: "row", alignItems: "center", gap: 12, marginBottom: 14 },
  title: { fontSize: 28, fontWeight: "800", color: colors.text },
  subtitle: { marginTop: 6, color: colors.textDim },
  headerRight: { alignItems: "flex-end", gap: 10 },
  balancePill: {
    ...glass.card,
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
    minWidth: 130,
  },
  balanceLabel: { color: colors.textDim, fontSize: 12, fontWeight: "800" },
  balanceValue: { marginTop: 2, color: colors.text, fontSize: 14, fontWeight: "900" },
  signOut: { color: colors.accent2, fontWeight: "800" },
  avatarRow: {
    flexDirection: "row",
    gap: 14,
    backgroundColor: colors.glassStrong,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: colors.glassBorder,
    padding: 16,
  },
  avatar: { width: 64, height: 64, borderRadius: 18, backgroundColor: colors.glassStrong },
  meta: { color: colors.textMuted, marginTop: 2 },
  sectionTitle: { marginTop: 18, color: colors.textDim, fontWeight: "800" },
  label: { marginTop: 14, marginBottom: 6, color: colors.textMuted, fontWeight: "700" },
  input: {
    backgroundColor: colors.glassStrong,
    color: colors.text,
    borderWidth: 1,
    borderColor: colors.glassBorder,
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  selectBox: {
    backgroundColor: colors.glassStrong,
    borderWidth: 1,
    borderColor: colors.glassBorder,
    borderRadius: 14,
    overflow: "hidden",
  },
  picker: { color: colors.text },
  actions: { marginTop: 20 },
});
