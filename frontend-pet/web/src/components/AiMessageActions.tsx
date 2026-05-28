import type { FeedbackType } from "../lib/chat-types";
import { isServerMessageId } from "../lib/chat-types";

type Props = {
  content: string;
  messageId: string;
  feedbackType: FeedbackType | null;
  disabled?: boolean;
  onCopy: () => void;
  onFeedback: (type: FeedbackType) => void;
  onRegenerate: () => void;
};

function IconCopy() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <rect x="9" y="9" width="13" height="13" rx="2" />
      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
    </svg>
  );
}

function IconThumbUp() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M7 10v12M15 5.88 14 10h5.83a2 2 0 0 1 1.92 2.56l-2.33 8A2 2 0 0 1 17.5 22H4a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h2.76a2 2 0 0 0 1.79-1.11L12 2a3.13 3.13 0 0 1 3 3.88Z" />
    </svg>
  );
}

function IconThumbDown() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M17 14V2M9 18.12 10 14H4.17a2 2 0 0 1-1.92-2.56l2.33-8A2 2 0 0 1 6.5 2H20a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-2.76a2 2 0 0 0-1.79 1.11L12 22a3.13 3.13 0 0 1-3-3.88Z" />
    </svg>
  );
}

function IconRefresh() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
      <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
      <path d="M3 3v5h5M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16" />
      <path d="M16 16h5v5" />
    </svg>
  );
}

export function AiMessageActions({
  content,
  messageId,
  feedbackType,
  disabled,
  onCopy,
  onFeedback,
  onRegenerate,
}: Props) {
  const serverId = isServerMessageId(messageId);

  return (
    <div className="msg-actions" aria-label="消息操作">
      <button
        type="button"
        className="msg-action-btn"
        title="复制"
        disabled={disabled || !content}
        onClick={onCopy}
      >
        <IconCopy />
        <span className="msg-action-label">复制</span>
      </button>
      {serverId && (
        <>
          <button
            type="button"
            className={`msg-action-btn${feedbackType === "LIKE" ? " active" : ""}`}
            title="点赞"
            disabled={disabled}
            onClick={() => onFeedback("LIKE")}
          >
            <IconThumbUp />
          </button>
          <button
            type="button"
            className={`msg-action-btn${feedbackType === "DISLIKE" ? " active" : ""}`}
            title="点踩"
            disabled={disabled}
            onClick={() => onFeedback("DISLIKE")}
          >
            <IconThumbDown />
          </button>
          <button
            type="button"
            className="msg-action-btn"
            title="重新生成"
            disabled={disabled}
            onClick={onRegenerate}
          >
            <IconRefresh />
            <span className="msg-action-label">重新生成</span>
          </button>
        </>
      )}
    </div>
  );
}
