export type AuthResponse = {
  userId: number;
  accessToken: string;
};

export type RegisterRequest = {
  login: string;
  password: string;
  email?: string | null;
  phoneNumber: string;
};

export type LoginRequest = {
  login: string;
  password: string;
};

export type PolifyPrincipal = {
  userId: number;
  login: string;
};

