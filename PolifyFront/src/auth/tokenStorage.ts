import * as SecureStore from "expo-secure-store";

const KEY = "polify.accessToken";

export async function getAccessToken(): Promise<string | null> {
  return SecureStore.getItemAsync(KEY);
}

export async function setAccessToken(token: string): Promise<void> {
  await SecureStore.setItemAsync(KEY, token);
}

export async function clearAccessToken(): Promise<void> {
  await SecureStore.deleteItemAsync(KEY);
}

