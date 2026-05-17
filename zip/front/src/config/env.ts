import { Platform } from "react-native";

// For real Android device in the same Wi-Fi network.
// PC IPv4 address: 10.14.96.245
export const API_BASE_URL =
  process.env.EXPO_PUBLIC_API_BASE_URL ??
  "http://10.14.96.245:8080";