import React from "react";
import { Button, StyleSheet, Text, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { AppStackParamList } from "../navigation/AppNavigator";

type Props = NativeStackScreenProps<AppStackParamList, "AttemptCompleted">;

export function AttemptCompletedScreen({ navigation, route }: Props) {
  const { surveyId } = route.params;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Completed</Text>
      <Text style={styles.subtitle}>Your attempt has been completed successfully.</Text>

      <View style={styles.actions}>
        <Button title="Back to surveys" onPress={() => navigation.popToTop()} />
      </View>

      <View style={styles.actions}>
        <Button title="Survey details" onPress={() => navigation.replace("SurveyDetails", { surveyId })} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: "center", backgroundColor: "#0B1220" },
  title: { fontSize: 28, fontWeight: "900", color: "#F8FAFC" },
  subtitle: { marginTop: 10, color: "#A5B4FC", lineHeight: 20 },
  actions: { marginTop: 16 },
});

