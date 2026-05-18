import { http } from "./http";
import { SurveyDetailsResponse, SurveyListItem } from "../types/survey";
import { CreateSurveyRequest, CreateSurveyResponse } from "../types/surveyAdmin";

export async function listSurveys(): Promise<SurveyListItem[]> {
  const resp = await http.get<SurveyListItem[]>("/surveys");
  return resp.data;
}

export async function getSurvey(id: number): Promise<SurveyDetailsResponse> {
  const resp = await http.get<SurveyDetailsResponse>(`/surveys/${id}`);
  return resp.data;
}

export async function createSurvey(req: CreateSurveyRequest): Promise<CreateSurveyResponse> {
  const resp = await http.post<CreateSurveyResponse>("/surveys", req);
  return resp.data;
}
