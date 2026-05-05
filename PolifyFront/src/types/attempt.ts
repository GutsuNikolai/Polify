export type StartAttemptRequest = {
  surveyId: number;
};

export type StartAttemptResponse = {
  attemptId: number;
};

export type ActiveAttemptResponse = {
  attemptId: number;
  surveyId: number;
  status: "IN_PROGRESS";
};

export type SubmitAnswerRequest = {
  questionId: number;
  textValue?: string | null;
  optionId?: number | null;
  optionIds?: number[] | null;
  priority?: { optionId: number; rank: number }[] | null;
};

export type AttemptDetailsResponse = {
  attemptId: number;
  surveyId: number;
  status: "IN_PROGRESS" | "COMPLETED" | "ABANDONED";
  startedAt: string;
  completedAt?: string | null;
  answers: AttemptAnswerDto[];
  nextQuestionId?: number | null;
};

export type AttemptAnswerDto = {
  answerId: number;
  questionId: number;
  answeredAt: string;
  textValue?: string | null;
  optionIds: number[];
  priority: { optionId: number; rank: number }[];
};

