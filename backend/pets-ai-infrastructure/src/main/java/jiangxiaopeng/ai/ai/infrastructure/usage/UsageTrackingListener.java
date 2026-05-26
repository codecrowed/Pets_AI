package jiangxiaopeng.ai.ai.infrastructure.usage;

import jiangxiaopeng.ai.conversation.domain.event.AiReplyCompletedEvent;
import jiangxiaopeng.ai.conversation.infrastructure.persistence.UsageRecordJpaEntity;
import jiangxiaopeng.ai.conversation.infrastructure.persistence.UsageRecordJpaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UsageTrackingListener {

    private final UsageRecordJpaRepository usageRecordRepository;

    public UsageTrackingListener(UsageRecordJpaRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    @EventListener
    public void onAiReplyCompleted(AiReplyCompletedEvent event) {
        UsageRecordJpaEntity record = new UsageRecordJpaEntity();
        record.setUserId(event.userId().value());
        record.setSessionId(event.sessionId());
        record.setModel(event.model());
        record.setTokensPrompt(event.tokenUsage().promptTokens());
        record.setTokensCompletion(event.tokenUsage().completionTokens());
        usageRecordRepository.save(record);
    }
}
