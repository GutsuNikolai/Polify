export type SurveyListItem = {
  id: number;
  title: string;
  description: string;
  rewardAmountBani: number;
  targetCompletions: number;
};

export type SurveyDetailsResponse = {
  id: number;
  title: string;
  description: string;
  rewardAmountBani: number;
  targetCompletions: number;
  questions: SurveyQuestionDto[];
};

export type SurveyQuestionType = "TEXT" | "RADIO" | "CHECKBOX" | "SELECT" | "PRIORITY";

export type SurveyQuestionDto = {
  id: number;
  type: SurveyQuestionType;
  text: string;
  position: number;
  required: boolean;
  options: SurveyOptionDto[];
};

export type SurveyOptionDto = {
  id: number;
  label: string;
  value: string;
  position: number;
  mediaUrl?: string | null;
};

