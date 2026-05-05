import { http } from "./http";
import { AuthResponse, LoginRequest, PolifyPrincipal, RegisterRequest } from "../types/auth";

export async function register(req: RegisterRequest): Promise<AuthResponse> {
  const resp = await http.post<AuthResponse>("/auth/register", req);
  return resp.data;
}

export async function login(req: LoginRequest): Promise<AuthResponse> {
  const resp = await http.post<AuthResponse>("/auth/login", req);
  return resp.data;
}

export async function me(): Promise<PolifyPrincipal> {
  const resp = await http.get<PolifyPrincipal>("/me");
  return resp.data;
}

