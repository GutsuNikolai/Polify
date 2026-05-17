import { http } from "./http";
import { SurveyDetailsResponse, SurveyListItem } from "../types/survey";

export async function listSurveys(): Promise<SurveyListItem[]> {
  const resp = await http.get<SurveyListItem[]>("/surveys");
  return resp.data;
}

export async function getSurvey(id: number): Promise<SurveyDetailsResponse> {
  const resp = await http.get<SurveyDetailsResponse>(`/surveys/${id}`);
  return resp.data;
}

