import { apiJson, authHeaders, getApiBase, messageFromFailedApiResponse } from "./api-client";
import { appendSseBuffer } from "./sse-parse";
import type { ChatListResponse, ChatSummaryDto, MessageListResponse } from "./chat-types";
import type { FeedbackType } from "./chat-types";

export async function createChat(body?: { title?: string | null; model?: string | null }): Promise<ChatSummaryDto> {
  return apiJson<ChatSummaryDto>("/api/v1/chat/createChat", {
    method: "POST",
    body: JSON.stringify(body ?? {}),
  });
}

export async function listChats(page = 0, size = 50): Promise<ChatListResponse> {
  const q = new URLSearchParams({ page: String(page), size: String(size) });
  return apiJson<ChatListResponse>(`/api/v1/chat/listChats?${q.toString()}`);
}

export async function searchChats(q: string, page = 0, size = 20): Promise<ChatListResponse> {
  const params = new URLSearchParams({ q, page: String(page), size: String(size) });
  return apiJson<ChatListResponse>(`/api/v1/chat/search?${params.toString()}`);
}

export async function listMessages(
  chatId: string,
  options?: { cursor?: string | null; size?: number }
): Promise<MessageListResponse> {
  const q = new URLSearchParams();
  if (options?.cursor) q.set("cursor", options.cursor);
  if (options?.size != null) q.set("size", String(options.size));
  const suffix = q.toString() ? `?${q.toString()}` : "";
  return apiJson<MessageListResponse>(`/api/v1/chats/${encodeURIComponent(chatId)}/messages${suffix}`);
}

export type StreamMessageHandlers = {
  onMessageStart?: (data: { messageId: string; role: string; model: string }) => void;
  onDelta?: (delta: string) => void;
  onMessageEnd?: (data: { messageId: string; finishReason: string; savedMessageUid?: string }) => void;
  onError?: (data: { code: string; message: string }) => void;
};

function dispatchSseEvent(
  e: { event: string; data: string },
  handlers: StreamMessageHandlers,
  streamFailedRef: { current: Error | null }
) {
  if (e.event === "ai_token") {
    if (e.data.length > 0) {
      handlers.onDelta?.(e.data);
    }
    return;
  }

  let parsed: unknown;
  try {
    parsed = JSON.parse(e.data) as unknown;
  } catch {
    return;
  }
  if (e.event === "message_start") {
    handlers.onMessageStart?.(parsed as { messageId: string; role: string; model: string });
  } else if (e.event === "content_delta") {
    const d = parsed as { delta?: string };
    if (typeof d.delta === "string" && d.delta.length > 0) {
      handlers.onDelta?.(d.delta);
    }
  } else if (e.event === "message_end") {
    handlers.onMessageEnd?.(parsed as { messageId: string; finishReason: string; savedMessageUid?: string });
  } else if (e.event === "error") {
    const err = parsed as { code?: string; message?: string };
    const message =
      typeof err.message === "string"
        ? err.message
        : typeof e.data === "string" && e.data.length > 0
          ? e.data
          : "Unknown error";
    handlers.onError?.({
      code: typeof err.code === "string" ? err.code : "ERROR",
      message,
    });
    streamFailedRef.current = new Error(message);
  }
}

async function readHttpErrorMessage(res: Response): Promise<string> {
  const text = await res.text();
  if (text) {
    try {
      const parsed: unknown = JSON.parse(text);
      const fromApi = messageFromFailedApiResponse(parsed);
      if (fromApi) return fromApi;
      if (typeof parsed === "object" && parsed !== null && "message" in parsed) {
        const m = (parsed as { message?: unknown }).message;
        if (typeof m === "string" && m.length > 0) return m;
      }
    } catch {
      return text;
    }
  }
  return res.statusText || `请求失败 (${res.status})`;
}

async function consumeSseResponse(res: Response, handlers: StreamMessageHandlers): Promise<void> {
  const reader = res.body?.getReader();
  if (!reader) {
    throw new Error("无法读取响应流");
  }

  const decoder = new TextDecoder();
  let sseRest = "";
  const streamFailedRef: { current: Error | null } = { current: null };

  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      const chunk = decoder.decode(value, { stream: true });
      const { events, rest } = appendSseBuffer(sseRest, chunk);
      sseRest = rest;
      for (const e of events) {
        dispatchSseEvent(e, handlers, streamFailedRef);
      }
    }
    if (sseRest.length > 0) {
      const { events } = appendSseBuffer(sseRest, "\n\n");
      for (const e of events) {
        dispatchSseEvent(e, handlers, streamFailedRef);
      }
    }
  } catch (e) {
    await reader.cancel().catch(() => {});
    throw e;
  }

  if (streamFailedRef.current) {
    throw streamFailedRef.current;
  }
}

export async function sendMessageStream(
  chatId: string,
  content: string,
  handlers: StreamMessageHandlers,
  attachmentIds: string[] | null = null,
  init?: { signal?: AbortSignal }
): Promise<void> {
  const headers = authHeaders();
  headers.set("Content-Type", "application/json");
  headers.set("Accept", "text/event-stream");
  const path = `/api/v1/chats/${encodeURIComponent(chatId)}/messages/stream`;
  const res = await fetch(`${getApiBase()}${path}`, {
    method: "POST",
    headers,
    body: JSON.stringify({ content, attachmentIds: attachmentIds ?? [] }),
    signal: init?.signal,
  });

  if (!res.ok) {
    throw new Error(await readHttpErrorMessage(res));
  }

  await consumeSseResponse(res, handlers);
}

export async function regenerateMessageStream(
  chatId: string,
  msgId: string,
  handlers: StreamMessageHandlers,
  init?: { signal?: AbortSignal }
): Promise<void> {
  const headers = authHeaders();
  headers.set("Accept", "text/event-stream");
  const path = `/api/v1/chats/${encodeURIComponent(chatId)}/messages/${encodeURIComponent(msgId)}/regenerate`;
  const res = await fetch(`${getApiBase()}${path}`, {
    method: "POST",
    headers,
    signal: init?.signal,
  });

  if (!res.ok) {
    throw new Error(await readHttpErrorMessage(res));
  }

  await consumeSseResponse(res, handlers);
}

export async function submitMessageFeedback(
  chatId: string,
  msgId: string,
  type: FeedbackType
): Promise<void> {
  await apiJson(`/api/v1/chats/${encodeURIComponent(chatId)}/messages/${encodeURIComponent(msgId)}/feedback`, {
    method: "POST",
    body: JSON.stringify({ type }),
  });
}

export async function removeMessageFeedback(chatId: string, msgId: string): Promise<void> {
  await apiJson(`/api/v1/chats/${encodeURIComponent(chatId)}/messages/${encodeURIComponent(msgId)}/feedback`, {
    method: "DELETE",
  });
}
