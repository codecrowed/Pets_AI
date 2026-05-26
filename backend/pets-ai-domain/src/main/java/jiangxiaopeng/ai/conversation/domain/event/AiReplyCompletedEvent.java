package jiangxiaopeng.ai.conversation.domain.event;

import jiangxiaopeng.ai.conversation.domain.model.TokenUsage;
import jiangxiaopeng.ai.shared.domain.DomainEvent;
import jiangxiaopeng.ai.shared.domain.vo.UserId;

import java.time.Instant;

public record AiReplyCompletedEvent(
        UserId userId,
        Long sessionId,
        String model,
        TokenUsage tokenUsage,
        Instant occurredAt
) implements DomainEvent {
    public AiReplyCompletedEvent(UserId userId, Long sessionId, String model, TokenUsage tokenUsage) {
        this(userId, sessionId, model, tokenUsage, Instant.now());
    }
}
