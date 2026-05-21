import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { SurveyListItem } from "../types/survey";
import { colors } from "../theme/colors";
import { glass } from "../theme/glass";

function baniToMdl(amountBani: number): string {
  const mdl = amountBani / 100;
  return `${mdl.toFixed(2)} MDL`;
}

export function SurveyCard({
  survey,
  onPress,
}: {
  survey: SurveyListItem;
  onPress: () => void;
}) {
  return (
    <Pressable onPress={onPress} style={({ pressed }) => [styles.card, pressed && styles.cardPressed]}>
      <View style={styles.sheenA} pointerEvents="none" />
      <View style={styles.sheenB} pointerEvents="none" />

      <View style={styles.cover}>
        <View style={styles.coverGlow} pointerEvents="none" />
        <View style={styles.coverBadge}>
          <Text style={styles.badgeText}>{baniToMdl(survey.rewardAmountBani)}</Text>
        </View>
        <Text style={styles.coverTitle} numberOfLines={2}>
          {survey.title}
        </Text>
      </View>

      <Text style={styles.desc} numberOfLines={2}>
        {survey.description}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    ...glass.card,
    borderRadius: 18,
    padding: 14,
    marginBottom: 12,
    overflow: "hidden",
  },
  cardPressed: {
    opacity: 0.92,
    transform: [{ scale: 0.995 }],
  },
  sheenA: {
    position: "absolute",
    top: -90,
    right: -90,
    width: 210,
    height: 210,
    borderRadius: 999,
    backgroundColor: colors.glassHighlight,
    opacity: 0.14,
  },
  sheenB: {
    position: "absolute",
    bottom: -120,
    left: -120,
    width: 260,
    height: 260,
    borderRadius: 999,
    backgroundColor: colors.accent2,
    opacity: 0.06,
  },
  cover: {
    height: 110,
    borderRadius: 14,
    padding: 14,
    backgroundColor: colors.glassStrong,
    borderWidth: 1,
    borderColor: colors.glassBorder,
    justifyContent: "space-between",
    overflow: "hidden",
  },
  coverGlow: {
    position: "absolute",
    top: -50,
    left: -30,
    width: 180,
    height: 180,
    borderRadius: 999,
    backgroundColor: colors.accent,
    opacity: 0.10,
  },
  coverBadge: {
    alignSelf: "flex-start",
    backgroundColor: "rgba(90, 169, 255, 0.85)",
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 999,
  },
  badgeText: {
    color: colors.text,
    fontWeight: "700",
  },
  coverTitle: {
    color: colors.text,
    fontSize: 18,
    fontWeight: "800",
    letterSpacing: 0.2,
  },
  desc: {
    marginTop: 10,
    color: colors.textMuted,
    lineHeight: 18,
  },
});
