import React, { useState } from "react";
import { Alert, Button, StyleSheet, Text, TextInput, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { register as apiRegister } from "../api/auth";
import { normalizeError } from "../api/http";
import { useAuthStore } from "../store/authStore";
import { AuthStackParamList } from "./LoginScreen";

type Props = NativeStackScreenProps<AuthStackParamList, "Register">;

export function RegisterScreen({ navigation }: Props) {
  const setSession = useAuthStore((s) => s.setSession);
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [busy, setBusy] = useState(false);

  const onSubmit = async () => {
    setBusy(true);
    try {
      const resp = await apiRegister({
        login: login.trim(),
        password,
        phoneNumber: phoneNumber.trim(),
      });
      await setSession(resp.accessToken);
    } catch (e) {
      const ne = normalizeError(e);
      if (ne.kind === "api") {
        Alert.alert("Registration failed", `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}`);
      } else {
        Alert.alert("Registration failed", ne.message);
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Create account</Text>
      <Text style={styles.subtitle}>We keep logs PII-free. Please enter a valid phone (E.164).</Text>

      <TextInput
        style={styles.input}
        placeholder="Login"
        autoCapitalize="none"
        value={login}
        onChangeText={setLogin}
      />
      <TextInput
        style={styles.input}
        placeholder="Password (min 8)"
        secureTextEntry
        value={password}
        onChangeText={setPassword}
      />
      <TextInput
        style={styles.input}
        placeholder="Phone number (E.164), e.g. +37369123456"
        autoCapitalize="none"
        keyboardType="phone-pad"
        value={phoneNumber}
        onChangeText={setPhoneNumber}
      />

      <View style={styles.actions}>
        <Button title={busy ? "Creating..." : "Create account"} disabled={busy} onPress={onSubmit} />
      </View>
      <View style={styles.actions}>
        <Button title="Back to login" disabled={busy} onPress={() => navigation.goBack()} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 24,
    justifyContent: "center",
    backgroundColor: "#0B1220",
  },
  title: {
    fontSize: 28,
    fontWeight: "700",
    color: "#F8FAFC",
  },
  subtitle: {
    fontSize: 14,
    marginTop: 6,
    marginBottom: 18,
    color: "#A5B4FC",
  },
  input: {
    backgroundColor: "#111A2E",
    color: "#F8FAFC",
    borderColor: "#1E2A4A",
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 10,
  },
  actions: {
    marginTop: 10,
  },
});
