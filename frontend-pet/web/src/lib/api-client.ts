import { getAccessToken } from "./auth-storage";
import { getCurrentPetId } from "./pet-storage";

export function getApiBase(): string {
  return (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8081/pet-ai-app").replace(/\/$/, "");
}

export function authHeaders(init: HeadersInit = {}): Headers {
  const headers = new Headers(init);
  const token = typeof window !== "undefined" ? getAccessToken() : null;
  if (token) headers.set("Authorization", `Bearer ${token}`);
  const petId = typeof window !== "undefined" ? getCurrentPetId() : null;
  if (petId) headers.set("X-Pet-Id", String(petId));
  return headers;
}

export type ApiResponse<T> = {
  code: string;
  message: string;
  success: boolean;
  timestamp: number;
  data: T;
};

function isApiResponseShape(v: unknown): v is ApiResponse<unknown> {
  return (
    typeof v === "object" &&
    v !== null &&
    "success" in v &&
    typeof (v as ApiResponse<unknown>).success === "boolean" &&
    "data" in v
  );
}

export function messageFromFailedApiResponse(raw: unknown): string | null {
  if (!isApiResponseShape(raw) || raw.success) return null;
  const msg = raw.message?.trim();
  if (msg) return msg;
  const code = raw.code?.trim();
  return code || null;
}

function messageFromUnknownBody(data: unknown): string | null {
  if (typeof data !== "object" || data === null) return null;
  if ("message" in data && typeof (data as { message?: unknown }).message === "string") {
    return (data as { message: string }).message;
  }
  return null;
}

export async function apiJson<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = typeof window !== "undefined" ? getAccessToken() : null;
  const petId = typeof window !== "undefined" ? getCurrentPetId() : null;
  const headers = new Headers(init.headers);
  if (init.body != null && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (token) headers.set("Authorization", `Bearer ${token}`);
  if (petId) headers.set("X-Pet-Id", String(petId));

  const res = await fetch(`${getApiBase()}${path.startsWith("/") ? path : `/${path}`}`, {
    ...init,
    headers,
  });

  const raw: unknown = await res.json().catch(() => ({}));

  if (isApiResponseShape(raw)) {
    if (!raw.success) {
      throw new Error(messageFromFailedApiResponse(raw) ?? "请求失败");
    }
    return raw.data as T;
  }

  if (!res.ok) {
    const msg = messageFromUnknownBody(raw) ?? res.statusText;
    throw new Error(msg || `请求失败 (${res.status})`);
  }
  return raw as T;
}
