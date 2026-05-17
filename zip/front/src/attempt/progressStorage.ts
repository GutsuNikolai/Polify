import * as SecureStore from "expo-secure-store";

function key(attemptId: number) {
  return `polify.attempt.${attemptId}.lastQuestionId`;
}

export async function getLastQuestionId(attemptId: number): Promise<number | null> {
  const v = await SecureStore.getItemAsync(key(attemptId));
  if (!v) return null;
  const n = Number(v);
  return Number.isFinite(n) ? n : null;
}

export async function setLastQuestionId(attemptId: number, questionId: number): Promise<void> {
  await SecureStore.setItemAsync(key(attemptId), String(questionId));
}

export async function clearAttemptProgress(attemptId: number): Promise<void> {
  await SecureStore.deleteItemAsync(key(attemptId));
}

