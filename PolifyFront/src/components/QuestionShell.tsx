import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors } from "../theme/colors";
import { glass } from "../theme/glass";

export function QuestionShell({
  title,
  progressLabel,
  onExit,
  children,
  footer,
}: {
  title: string;
  progressLabel: string;
  onExit: () => void;
  children: React.ReactNode;
  footer: React.ReactNode;
}) {
  const insets = useSafeAreaInsets();

  return (
    <View style={styles.container}>
      <View style={[styles.header, { paddingTop: Math.max(14, 14 + insets.top) }]}>
        <View style={{ flex: 1 }}>
          <Text style={styles.progress}>{progressLabel}</Text>
          <Text style={styles.title}>{title}</Text>
        </View>
        <Pressable onPress={onExit} style={({ pressed }) => [styles.exitBtn, pressed && { opacity: 0.85 }]}>
          <Text style={styles.exitText}>Exit</Text>
        </Pressable>
      </View>

      <View style={styles.body}>{children}</View>
      <View style={[styles.footer, { paddingBottom: Math.max(14, 14 + insets.bottom) }]}>{footer}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg1 },
  header: {
    paddingTop: 14,
    paddingHorizontal: 18,
    paddingBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  progress: { color: colors.textDim, fontWeight: "700" },
  title: { marginTop: 6, color: colors.text, fontSize: 18, fontWeight: "800", lineHeight: 24 },
  exitBtn: {
    ...glass.card,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  exitText: { color: colors.text, fontWeight: "700" },
  body: { flex: 1, padding: 18 },
  footer: {
    paddingHorizontal: 18,
    paddingVertical: 14,
    borderTopWidth: 1,
    borderTopColor: colors.line,
    backgroundColor: colors.bg1,
  },
});
