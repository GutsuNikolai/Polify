import React from "react";
import { Button, StyleSheet, Text, View } from "react-native";
import { useAuthStore } from "../store/authStore";

export function HomeScreen() {
  const user = useAuthStore((s) => s.user);
  const signOut = useAuthStore((s) => s.signOut);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Welcome</Text>
      <Text style={styles.subtitle}>
        {user ? `login: ${user.login} (id: ${user.userId})` : "No user loaded"}
      </Text>
      <View style={styles.actions}>
        <Button title="Sign out" onPress={() => signOut()} />
      </View>
      <Text style={styles.note}>
        Next: Surveys list and attempt runner UI.
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: "center", backgroundColor: "#0B1220" },
  title: { fontSize: 28, fontWeight: "700", color: "#F8FAFC" },
  subtitle: { marginTop: 8, color: "#C7D2FE" },
  note: { marginTop: 18, color: "#94A3B8" },
  actions: { marginTop: 16 },
});

