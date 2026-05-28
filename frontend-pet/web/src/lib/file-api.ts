import { authHeaders, getApiBase, messageFromFailedApiResponse } from "./api-client";
import type { ApiResponse } from "./api-client";

export type FileUploadResponse = {
  id: number;
  name: string;
  contentType: string;
  fileSize: number;
  status: string;
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

export async function uploadFile(file: File, init?: { signal?: AbortSignal }): Promise<FileUploadResponse> {
  const form = new FormData();
  form.append("file", file);
  const headers = authHeaders();
  const res = await fetch(`${getApiBase()}/api/v1/files/upload`, {
    method: "POST",
    headers,
    body: form,
    signal: init?.signal,
  });
  const raw: unknown = await res.json().catch(() => ({}));
  if (isApiResponseShape(raw)) {
    if (!raw.success) {
      throw new Error(messageFromFailedApiResponse(raw) ?? "上传失败");
    }
    return raw.data as FileUploadResponse;
  }
  if (!res.ok) {
    throw new Error(`上传失败 (${res.status})`);
  }
  return raw as FileUploadResponse;
}
