package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usage_records")
@Getter
@Setter
public class UsageRecordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", nullable = false)
    private Long uid;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "tokens_prompt", nullable = false)
    private int tokensPrompt;

    @Column(name = "tokens_completion", nullable = false)
    private int tokensCompletion;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) recordedAt = Instant.now();
    }

}
