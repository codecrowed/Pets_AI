package jiangxiaopeng.ai.shared;

import jiangxiaopeng.ai.shared.domain.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
