import React, { useState } from "react";
import { Alert, Button, StyleSheet, Text, TextInput, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { login as apiLogin } from "../api/auth";
import { normalizeError } from "../api/http";
import { useAuthStore } from "../store/authStore";

export type AuthStackParamList = {
  Login: undefined;
  Register: undefined;
};

type Props = NativeStackScreenProps<AuthStackParamList, "Login">;

export function LoginScreen({ navigation }: Props) {
  const setSession = useAuthStore((s) => s.setSession);
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);

  const onSubmit = async () => {
    setBusy(true);
    try {
      const resp = await apiLogin({ login: login.trim(), password });
      await setSession(resp.accessToken);
    } catch (e) {
      const ne = normalizeError(e);
      if (ne.kind === "api") {
        Alert.alert("Login failed", `${ne.apiError.message}\nrequestId: ${ne.apiError.requestId}`);
      } else {
        Alert.alert("Login failed", ne.message);
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Polify</Text>
      <Text style={styles.subtitle}>Sign in</Text>

      <TextInput
        style={styles.input}
        placeholder="Login"
        autoCapitalize="none"
        value={login}
        onChangeText={setLogin}
      />
      <TextInput
        style={styles.input}
        placeholder="Password"
        secureTextEntry
        value={password}
        onChangeText={setPassword}
      />

      <View style={styles.actions}>
        <Button title={busy ? "Signing in..." : "Sign in"} disabled={busy} onPress={onSubmit} />
      </View>

      <View style={styles.actions}>
        <Button title="Create account" disabled={busy} onPress={() => navigation.navigate("Register")} />
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
    fontSize: 34,
    fontWeight: "700",
    color: "#F8FAFC",
  },
  subtitle: {
    fontSize: 16,
    marginTop: 6,
    marginBottom: 18,
    color: "#C7D2FE",
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

