import { create } from "zustand";
import { clearAccessToken, getAccessToken, setAccessToken } from "../auth/tokenStorage";
import { me as apiMe } from "../api/auth";
import { PolifyPrincipal } from "../types/auth";

export type AuthStatus = "loading" | "authenticated" | "unauthenticated";

type AuthState = {
  status: AuthStatus;
  accessToken: string | null;
  user: PolifyPrincipal | null;
  bootstrap: () => Promise<void>;
  setSession: (token: string) => Promise<void>;
  signOut: () => Promise<void>;
};

export const useAuthStore = create<AuthState>((set, get) => ({
  status: "loading",
  accessToken: null,
  user: null,

  bootstrap: async () => {
    const token = await getAccessToken();
    if (!token) {
      set({ status: "unauthenticated", accessToken: null, user: null });
      return;
    }

    set({ accessToken: token });
    try {
      const user = await apiMe();
      set({ status: "authenticated", user });
    } catch {
      await clearAccessToken();
      set({ status: "unauthenticated", accessToken: null, user: null });
    }
  },

  setSession: async (token: string) => {
    await setAccessToken(token);
    set({ accessToken: token });
    const user = await apiMe();
    set({ status: "authenticated", user });
  },

  signOut: async () => {
    await clearAccessToken();
    set({ status: "unauthenticated", accessToken: null, user: null });
  },
}));

