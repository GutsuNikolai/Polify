import { http } from "./http";
import { UpdateUserProfileRequest, UserProfileResponse } from "../types/profile";

export async function getProfile(): Promise<UserProfileResponse> {
  const resp = await http.get<UserProfileResponse>("/profile");
  return resp.data;
}

export async function updateProfile(req: UpdateUserProfileRequest): Promise<UserProfileResponse> {
  const resp = await http.put<UserProfileResponse>("/profile", req);
  return resp.data;
}

