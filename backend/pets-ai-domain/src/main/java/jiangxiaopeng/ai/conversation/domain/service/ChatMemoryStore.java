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
    Message saveUserMessage(Long uid, Long sessionId, String agentId, String content);

    /**
     * Save a pending AI message (status=STREAMING).
     */
    Message savePendingAiMessage(Long uid,Long sessionId, String agentId, String model);

    /**
     * Complete an AI message with full content and save (status=COMPLETED).
     */
    Message completeAiMessage(Message aiMsg, String fullContent);
}
