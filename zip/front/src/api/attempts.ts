import { http } from "./http";
import {
  ActiveAttemptResponse,
  AttemptDetailsResponse,
  StartAttemptResponse,
  SubmitAnswerRequest,
} from "../types/attempt";

export async function startAttempt(surveyId: number): Promise<StartAttemptResponse> {
  const resp = await http.post<StartAttemptResponse>("/attempts/start", { surveyId });
  return resp.data;
}

export async function submitAnswer(attemptId: number, req: SubmitAnswerRequest): Promise<void> {
  await http.post(`/attempts/${attemptId}/answers`, req);
}

export async function completeAttempt(attemptId: number): Promise<void> {
  await http.post(`/attempts/${attemptId}/complete`);
}

export async function getAttempt(attemptId: number): Promise<AttemptDetailsResponse> {
  const resp = await http.get<AttemptDetailsResponse>(`/attempts/${attemptId}`);
  return resp.data;
}

export async function listAttempts(surveyId?: number): Promise<AttemptDetailsResponse[]> {
  const qs = surveyId ? `?surveyId=${surveyId}` : "";
  const resp = await http.get<AttemptDetailsResponse[]>(`/attempts${qs}`);
  return resp.data;
}

export async function getActiveAttempt(surveyId: number): Promise<ActiveAttemptResponse | null> {
  // Backend returns null body when no active attempt exists.
  const resp = await http.get<ActiveAttemptResponse | null>(`/attempts/active?surveyId=${surveyId}`, {
    // Avoid JSON parse issues if body is empty.
    transformResponse: [
      (data) => {
        if (data == null || data === "") return null;
        return JSON.parse(data);
      },
    ],
  });
  return resp.data;
}
