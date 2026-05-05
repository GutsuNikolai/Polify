import axios, { AxiosError } from "axios";
import { API_BASE_URL } from "../config/env";
import { ApiError, isApiError } from "../types/apiError";
import { getAccessToken } from "../auth/tokenStorage";

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  return (async () => {
    const token = await getAccessToken();
    if (token) {
      config.headers = config.headers ?? {};
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (config.headers as any).Authorization = `Bearer ${token}`;
    }
    return config;
  })();
});

export type NormalizedApiError = {
  kind: "api";
  apiError: ApiError;
};

export type NormalizedNetworkError = {
  kind: "network";
  message: string;
};

export type NormalizedUnknownError = {
  kind: "unknown";
  message: string;
};

export type NormalizedError = NormalizedApiError | NormalizedNetworkError | NormalizedUnknownError;

export function normalizeError(err: unknown): NormalizedError {
  if (axios.isAxiosError(err)) {
    const ax = err as AxiosError;
    const data = ax.response?.data;
    if (isApiError(data)) {
      return { kind: "api", apiError: data };
    }
    if (!ax.response) {
      return { kind: "network", message: "Network error. Check connection and backend URL." };
    }
    return { kind: "unknown", message: "Unexpected server response." };
  }
  return { kind: "unknown", message: "Unexpected error." };
}
