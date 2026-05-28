import { apiJson, getApiBase } from "./api-client";
import {
  clearAllAuthStorage,
  getAccessToken,
  getRefreshToken,
  getUserJson,
  persistAuthTokens,
} from "./auth-storage";

export type UserInfo = {
  uid: number;
  username: string;
  email: string;
  avatarUrl: string | null;
  plan: string;
};

export type AuthPayload = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: UserInfo;
};

export function readStoredUser(): UserInfo | null {
  if (typeof window === "undefined" || !getAccessToken()) return null;
  const raw = getUserJson();
  if (!raw) return null;
  try {
    return JSON.parse(raw) as UserInfo;
  } catch {
    return null;
  }
}

/** @param rememberMe 持久化到 localStorage；否则仅 sessionStorage（关闭浏览器后需重新登录） */
export function persistAuth(payload: AuthPayload, rememberMe: boolean): void {
  persistAuthTokens(rememberMe, {
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
    userJson: JSON.stringify(payload.user),
  });
}

export function clearAuth(): void {
  clearAllAuthStorage();
}

export async function login(email: string, password: string): Promise<AuthPayload> {
  return apiJson<AuthPayload>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify({ email: email.trim(), password }),
  });
}

export type RegisterBody = {
  email: string;
  inviteCode: string;
  password: string;
  username: string;
};

export async function register(body: RegisterBody): Promise<AuthPayload> {
  return apiJson<AuthPayload>("/api/v1/auth/register", {
    method: "POST",
    body: JSON.stringify({
      email: body.email.trim(),
      inviteCode: body.inviteCode.trim(),
      password: body.password,
      username: body.username.trim(),
    }),
  });
}

export async function logoutRemote(): Promise<void> {
  const refresh = getRefreshToken();
  const headers = new Headers();
  headers.set("Content-Type", "application/json");
  const token = getAccessToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);
  try {
    await fetch(`${getApiBase()}/api/v1/auth/logout`, {
      method: "POST",
      headers,
      body: JSON.stringify({ refreshToken: refresh ?? "" }),
    });
  } catch {
    /* still clear local session */
  } finally {
    clearAuth();
  }
}
