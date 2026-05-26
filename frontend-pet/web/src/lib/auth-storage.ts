import { AUTH_ACCESS_TOKEN_KEY, AUTH_REFRESH_TOKEN_KEY, AUTH_USER_JSON_KEY } from "./auth-keys";

function clearAuthFromStorage(s: Storage): void {
  s.removeItem(AUTH_ACCESS_TOKEN_KEY);
  s.removeItem(AUTH_REFRESH_TOKEN_KEY);
  s.removeItem(AUTH_USER_JSON_KEY);
}

/** Read token: persistent (local) session first, then tab-only (session) */
export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(AUTH_ACCESS_TOKEN_KEY) ?? sessionStorage.getItem(AUTH_ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(AUTH_REFRESH_TOKEN_KEY) ?? sessionStorage.getItem(AUTH_REFRESH_TOKEN_KEY);
}

/** User JSON lives alongside tokens in the same storage */
export function getUserJson(): string | null {
  if (typeof window === "undefined") return null;
  if (localStorage.getItem(AUTH_ACCESS_TOKEN_KEY)) {
    return localStorage.getItem(AUTH_USER_JSON_KEY);
  }
  if (sessionStorage.getItem(AUTH_ACCESS_TOKEN_KEY)) {
    return sessionStorage.getItem(AUTH_USER_JSON_KEY);
  }
  return null;
}

/**
 * @param rememberMe true → localStorage（持久化）；false → sessionStorage（关闭浏览器后失效）
 */
export function persistAuthTokens(
  rememberMe: boolean,
  payload: { accessToken: string; refreshToken: string; userJson: string },
): void {
  clearAuthFromStorage(localStorage);
  clearAuthFromStorage(sessionStorage);
  const target = rememberMe ? localStorage : sessionStorage;
  target.setItem(AUTH_ACCESS_TOKEN_KEY, payload.accessToken);
  target.setItem(AUTH_REFRESH_TOKEN_KEY, payload.refreshToken);
  target.setItem(AUTH_USER_JSON_KEY, payload.userJson);
}

export function clearAllAuthStorage(): void {
  clearAuthFromStorage(localStorage);
  clearAuthFromStorage(sessionStorage);
}
