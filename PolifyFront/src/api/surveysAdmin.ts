import { http } from "./http";
import { ManageSurveyListItem } from "../types/surveyAdmin";

export async function listAllSurveysForManage(): Promise<ManageSurveyListItem[]> {
  const resp = await http.get<ManageSurveyListItem[]>("/surveys/manage");
  return resp.data;
}

export async function archiveSurvey(surveyId: number): Promise<void> {
  await http.post(`/surveys/${surveyId}/archive`);
}

