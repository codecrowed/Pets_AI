export type MessageDto = {
  id: string;
  role: string;
  content: string;
  codeBlock: { language: string; code: string } | null;
  feedbackType: string | null;
  model: string | null;
  createdAt: string;
};

export type MessageListResponse = {
  messages: MessageDto[];
  nextCursor: string | null;
  hasMore: boolean;
};

export type ChatSummaryDto = {
  id: string;
  title: string | null;
  updatedAt: string;
};

export type ChatListResponse = {
  groups: { dateLabel: string; chats: ChatSummaryDto[] }[];
  pageInfo: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
  };
};

export type ChatMessage = {
  id: string;
  role: "user" | "ai";
  content: string;
  codeBlock?: { language: string; code: string };
};

export function messageDtoToUi(m: MessageDto): ChatMessage {
  const r = (m.role || "").toUpperCase();
  const role = r === "USER" ? "user" : "ai";
  return {
    id: m.id,
    role,
    content: m.content ?? "",
    codeBlock: m.codeBlock ? { language: m.codeBlock.language, code: m.codeBlock.code } : undefined,
  };
}
