export type Gender = "MALE" | "FEMALE" | null;
export type Role = "USER" | "MODERATOR" | "ADMIN";

export type UserProfileResponse = {
  id: number;
  login: string;
  email?: string | null;
  phoneNumber: string;
  fullName?: string | null;
  gender?: Gender;
  birthDate?: string | null; // YYYY-MM-DD
  country?: string | null;
  city?: string | null;
  role: Role;
  verified: boolean;
  lastActiveAt: string;
  createdAt: string;
};

export type UpdateUserProfileRequest = {
  email?: string | null;
  fullName?: string | null;
  gender?: Gender;
  birthDate?: string | null; // YYYY-MM-DD
  country?: string | null;
  city?: string | null;
};
