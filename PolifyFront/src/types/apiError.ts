export type FieldErrorResponse = {
  field: string | null;
  message: string;
};

export type ErrorCode =
  | "INTERNAL_ERROR"
  | "VALIDATION_FAILED"
  | "MALFORMED_JSON"
  | "TYPE_MISMATCH"
  | "ENDPOINT_NOT_FOUND"
  | "METHOD_NOT_ALLOWED"
  | "UNAUTHORIZED"
  | "FORBIDDEN"
  | "LOGIN_ALREADY_EXISTS"
  | "EMAIL_ALREADY_EXISTS"
  | "PHONE_ALREADY_EXISTS"
  | "INVALID_CREDENTIALS"
  | "SURVEY_NOT_FOUND"
  | "QUESTION_NOT_FOUND"
  | "ATTEMPT_NOT_FOUND"
  | "ATTEMPT_NOT_ALLOWED"
  | "ATTEMPT_VALIDATION";

export type ApiError = {
  timestamp: string;
  status: number;
  error: string;
  code: ErrorCode;
  message: string;
  path: string;
  requestId: string;
  details: FieldErrorResponse[];
};

export function isApiError(value: unknown): value is ApiError {
  if (!value || typeof value !== "object") return false;
  const v = value as any;
  return (
    typeof v.timestamp === "string" &&
    typeof v.status === "number" &&
    typeof v.error === "string" &&
    typeof v.code === "string" &&
    typeof v.message === "string" &&
    typeof v.path === "string" &&
    typeof v.requestId === "string" &&
    Array.isArray(v.details)
  );
}

