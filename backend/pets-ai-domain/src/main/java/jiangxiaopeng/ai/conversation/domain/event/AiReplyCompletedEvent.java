package jiangxiaopeng.ai.conversation.domain.event;

import java.time.Instant;

import jiangxiaopeng.ai.conversation.domain.model.TokenUsage;
import jiangxiaopeng.ai.shared.domain.DomainEvent;

public record AiReplyCompletedEvent(
        Long uid,
        Long sessionId,
        String model,
        TokenUsage tokenUsage,
        Instant occurredAt
) implements DomainEvent {
    public AiReplyCompletedEvent(Long uid, Long sessionId, String model, TokenUsage tokenUsage) {
        this(uid, sessionId, model, tokenUsage, Instant.now());
    }
}
