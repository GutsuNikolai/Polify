import { SurveyQuestionType } from "./survey";

export type CreateSurveyOption = {
  label: string;
  value: string;
  position: number;
  mediaUrl?: string | null;
};

export type CreateSurveyQuestion = {
  type: SurveyQuestionType;
  text: string;
  position: number;
  required: boolean;
  options?: CreateSurveyOption[];
};

export type CreateSurveyRequest = {
  title: string;
  description?: string | null;
  rewardAmountBani: number;
  targetCompletions: number;
  questions: CreateSurveyQuestion[];
};

export type CreateSurveyResponse = {
  surveyId: number;
};

