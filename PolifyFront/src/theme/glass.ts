import { StyleSheet } from "react-native";
import { colors } from "./colors";

export const glass = StyleSheet.create({
  card: {
    backgroundColor: colors.glass,
    borderWidth: 1,
    borderColor: colors.glassBorder,
    borderRadius: 18,
    shadowColor: "#000",
    shadowOpacity: 0.22,
    shadowRadius: 18,
    shadowOffset: { width: 0, height: 10 },
    elevation: 6,
  },
  inner: {
    borderRadius: 18,
    overflow: "hidden",
  },
  sheen: {
    position: "absolute",
    top: -60,
    left: -60,
    width: 220,
    height: 220,
    borderRadius: 110,
    backgroundColor: colors.glassHighlight,
    opacity: 0.18,
  },
});

