import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { SurveyListItem } from "../types/survey";

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
      <View style={styles.cover}>
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
    backgroundColor: "#0F172A",
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "#1F2A44",
    padding: 14,
    marginBottom: 12,
  },
  cardPressed: {
    opacity: 0.92,
    transform: [{ scale: 0.995 }],
  },
  cover: {
    height: 110,
    borderRadius: 14,
    padding: 14,
    backgroundColor: "#0B1220",
    borderWidth: 1,
    borderColor: "#243150",
    justifyContent: "space-between",
    overflow: "hidden",
  },
  coverBadge: {
    alignSelf: "flex-start",
    backgroundColor: "#1D4ED8",
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 999,
  },
  badgeText: {
    color: "#EFF6FF",
    fontWeight: "700",
  },
  coverTitle: {
    color: "#F8FAFC",
    fontSize: 18,
    fontWeight: "800",
    letterSpacing: 0.2,
  },
  desc: {
    marginTop: 10,
    color: "#CBD5E1",
    lineHeight: 18,
  },
});

