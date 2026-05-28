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
  chatId: string;
  uid: number;
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

export type FeedbackType = "LIKE" | "DISLIKE";

export type ChatMessage = {
  id: string;
  role: "user" | "ai";
  content: string;
  codeBlock?: { language: string; code: string };
  feedbackType?: FeedbackType | null;
};

export function messageDtoToUi(m: MessageDto): ChatMessage {
  const r = (m.role || "").toUpperCase();
  const role = r === "USER" ? "user" : "ai";
  const ft = m.feedbackType;
  const feedbackType: FeedbackType | null =
    ft === "LIKE" || ft === "DISLIKE" ? ft : null;
  return {
    id: m.id,
    role,
    content: m.content ?? "",
    codeBlock: m.codeBlock ? { language: m.codeBlock.language, code: m.codeBlock.code } : undefined,
    feedbackType,
  };
}

export function isServerMessageId(id: string): boolean {
  return !id.startsWith("local-") && !id.startsWith("ai-");
}
