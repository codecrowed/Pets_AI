package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "usage_records")
public class UsageRecordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getTokensPrompt() { return tokensPrompt; }
    public void setTokensPrompt(int tokensPrompt) { this.tokensPrompt = tokensPrompt; }
    public int getTokensCompletion() { return tokensCompletion; }
    public void setTokensCompletion(int tokensCompletion) { this.tokensCompletion = tokensCompletion; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
