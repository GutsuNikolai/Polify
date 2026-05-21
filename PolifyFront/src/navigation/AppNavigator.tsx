import React from "react";
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { LoginScreen, AuthStackParamList } from "../screens/LoginScreen";
import { RegisterScreen } from "../screens/RegisterScreen";
import { SurveysListScreen } from "../screens/SurveysListScreen";
import { SurveyDetailsScreen } from "../screens/SurveyDetailsScreen";
import { AttemptRunnerScreen } from "../screens/AttemptRunnerScreen";
import { AttemptCompletedScreen } from "../screens/AttemptCompletedScreen";
import { ProfileScreen } from "../screens/ProfileScreen";
import { useAuthStore } from "../store/authStore";
import { ActivityIndicator, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { ManageSurveysScreen, ManageStackParamList } from "../screens/manage/ManageSurveysScreen";
import { CreateSurveyScreen } from "../screens/manage/CreateSurveyScreen";
import { ManageExistingSurveysScreen } from "../screens/manage/ManageExistingSurveysScreen";
import { colors } from "../theme/colors";

export type AppStackParamList = {
  Surveys: undefined;
  SurveyDetails: { surveyId: number };
  Attempt: { surveyId: number; attemptId: number };
  AttemptCompleted: { surveyId: number; attemptId: number };
};

const AuthStack = createNativeStackNavigator<AuthStackParamList>();
const SurveysStack = createNativeStackNavigator<AppStackParamList>();
const Tabs = createBottomTabNavigator<{ SurveysTab: undefined; ManageTab: undefined; ProfileTab: undefined }>();
const ManageStack = createNativeStackNavigator<ManageStackParamList>();

function SurveysStackNavigator() {
  return (
    <SurveysStack.Navigator>
      <SurveysStack.Screen name="Surveys" component={SurveysListScreen} options={{ headerShown: false }} />
      <SurveysStack.Screen
        name="SurveyDetails"
        component={SurveyDetailsScreen}
        options={{ title: "Survey", headerTintColor: colors.text, headerStyle: { backgroundColor: colors.bg1 } }}
      />
      <SurveysStack.Screen name="Attempt" component={AttemptRunnerScreen} options={{ headerShown: false }} />
      <SurveysStack.Screen
        name="AttemptCompleted"
        component={AttemptCompletedScreen}
        options={{ title: "Completed", headerTintColor: colors.text, headerStyle: { backgroundColor: colors.bg1 } }}
      />
    </SurveysStack.Navigator>
  );
}

function ManageStackNavigator() {
  return (
    <ManageStack.Navigator>
      <ManageStack.Screen name="ManageHome" component={ManageSurveysScreen} options={{ headerShown: false }} />
      <ManageStack.Screen
        name="ManageExisting"
        component={ManageExistingSurveysScreen}
        options={{ title: "Existing surveys", headerTintColor: colors.text, headerStyle: { backgroundColor: colors.bg1 } }}
      />
      <ManageStack.Screen
        name="CreateSurvey"
        component={CreateSurveyScreen}
        options={{ title: "Create survey", headerTintColor: colors.text, headerStyle: { backgroundColor: colors.bg1 } }}
      />
    </ManageStack.Navigator>
  );
}

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
        <Tabs.Navigator
          screenOptions={({ route }) => ({
            headerShown: false,
            tabBarStyle: { backgroundColor: colors.bg1, borderTopColor: colors.line },
            tabBarActiveTintColor: colors.accent2,
            tabBarInactiveTintColor: "rgba(198, 211, 245, 0.55)",
            tabBarIcon: ({ color, size }) => {
              const name =
                route.name === "SurveysTab" ? "list" : route.name === "ManageTab" ? "construct" : "person";
              return <Ionicons name={name} size={size} color={color} />;
            },
          })}
        >
          <Tabs.Screen name="SurveysTab" component={SurveysStackNavigator} options={{ title: "Surveys" }} />
          <Tabs.Screen name="ManageTab" component={ManageStackNavigator} options={{ title: "Manage" }} />
          <Tabs.Screen name="ProfileTab" component={ProfileScreen} options={{ title: "Profile" }} />
        </Tabs.Navigator>
      ) : (
        <AuthStack.Navigator>
          <AuthStack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
          <AuthStack.Screen name="Register" component={RegisterScreen} options={{ headerShown: false }} />
        </AuthStack.Navigator>
      )}
    </NavigationContainer>
  );
}
