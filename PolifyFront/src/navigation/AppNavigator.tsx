import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { LoginScreen, AuthStackParamList } from "../screens/LoginScreen";
import { RegisterScreen } from "../screens/RegisterScreen";
import { SurveysListScreen } from "../screens/SurveysListScreen";
import { SurveyDetailsScreen } from "../screens/SurveyDetailsScreen";
import { AttemptRunnerScreen } from "../screens/AttemptRunnerScreen";
import { AttemptCompletedScreen } from "../screens/AttemptCompletedScreen";
import { useAuthStore } from "../store/authStore";
import { ActivityIndicator, View } from "react-native";

export type AppStackParamList = {
  Surveys: undefined;
  SurveyDetails: { surveyId: number };
  Attempt: { surveyId: number; attemptId: number };
  AttemptCompleted: { surveyId: number; attemptId: number };
};

const AuthStack = createNativeStackNavigator<AuthStackParamList>();
const AppStack = createNativeStackNavigator<AppStackParamList>();

export function AppNavigator() {
  const status = useAuthStore((s) => s.status);

  if (status === "loading") {
    return (
      <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
        <ActivityIndicator />
      </View>
    );
  }

  return (
    <NavigationContainer>
      {status === "authenticated" ? (
        <AppStack.Navigator>
          <AppStack.Screen name="Surveys" component={SurveysListScreen} options={{ headerShown: false }} />
          <AppStack.Screen
            name="SurveyDetails"
            component={SurveyDetailsScreen}
            options={{ title: "Survey", headerTintColor: "#E2E8F0", headerStyle: { backgroundColor: "#0B1220" } }}
          />
          <AppStack.Screen
            name="Attempt"
            component={AttemptRunnerScreen}
            options={{ headerShown: false }}
          />
          <AppStack.Screen
            name="AttemptCompleted"
            component={AttemptCompletedScreen}
            options={{ title: "Completed", headerTintColor: "#E2E8F0", headerStyle: { backgroundColor: "#0B1220" } }}
          />
        </AppStack.Navigator>
      ) : (
        <AuthStack.Navigator>
          <AuthStack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
          <AuthStack.Screen name="Register" component={RegisterScreen} options={{ headerShown: false }} />
        </AuthStack.Navigator>
      )}
    </NavigationContainer>
  );
}
