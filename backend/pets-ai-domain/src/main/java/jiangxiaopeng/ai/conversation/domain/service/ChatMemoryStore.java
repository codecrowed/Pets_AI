package jiangxiaopeng.ai.conversation.domain.service;

import jiangxiaopeng.ai.conversation.domain.model.Message;

/**
 * Domain interface for chat message persistence.
 * Centralizes all message write operations.
 */
public interface ChatMemoryStore {

    /**
     * Save a user message.
     */
    Message saveUserMessage(Long sessionId, String content);

    /**
     * Save a pending AI message (status=STREAMING).
     */
    Message savePendingAiMessage(Long sessionId, String model);

    /**
     * Complete an AI message with full content and save (status=COMPLETED).
     */
    Message completeAiMessage(Message aiMsg, String fullContent);
}
