import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";

export function OptionRow({
  label,
  selected,
  onPress,
  right,
  variant = "radio",
}: {
  label: string;
  selected: boolean;
  onPress: () => void;
  right?: React.ReactNode;
  variant?: "radio" | "checkbox";
}) {
  return (
    <Pressable onPress={onPress} style={({ pressed }) => [styles.row, pressed && styles.pressed]}>
      {variant === "radio" ? (
        <View style={[styles.radioDot, selected && styles.radioDotSelected]} />
      ) : (
        <View style={[styles.checkboxBox, selected && styles.checkboxBoxSelected]}>
          {selected ? <Text style={styles.checkboxTick}>✓</Text> : null}
        </View>
      )}
      <Text style={styles.label}>{label}</Text>
      <View style={{ flex: 1 }} />
      {right}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 12,
    paddingHorizontal: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#1F2A44",
    backgroundColor: "#0F172A",
    marginBottom: 10,
    gap: 10,
  },
  pressed: { opacity: 0.9 },
  radioDot: {
    width: 14,
    height: 14,
    borderRadius: 7,
    borderWidth: 2,
    borderColor: "#64748B",
  },
  radioDotSelected: { borderColor: "#60A5FA", backgroundColor: "#60A5FA" },
  checkboxBox: {
    width: 18,
    height: 18,
    borderRadius: 4,
    borderWidth: 2,
    borderColor: "#64748B",
    alignItems: "center",
    justifyContent: "center",
  },
  checkboxBoxSelected: {
    borderColor: "#34D399",
    backgroundColor: "#34D399",
  },
  checkboxTick: {
    color: "#052E16",
    fontWeight: "900",
    fontSize: 14,
    lineHeight: 16,
  },
  label: { color: "#E2E8F0", fontWeight: "600", flexShrink: 1 },
});
