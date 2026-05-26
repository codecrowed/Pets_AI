package jiangxiaopeng.ai.identity.domain.model;

import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;

import java.time.Instant;

public class InvitationCode {

    private Long id;
    private String code;
    private int maxUses;
    private int usedCount;
    private Instant expiresAt;
    private String status;
    private Instant createdAt;

    public InvitationCode() {}

    public void validate() {
        if (!"ACTIVE".equals(status)) {
            throw new BusinessException(ErrorCode.AUTH_001);
        }
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            throw new BusinessException(ErrorCode.AUTH_001);
        }
        if (usedCount >= maxUses) {
            throw new BusinessException(ErrorCode.AUTH_001);
        }
    }

    public void use() {
        validate();
        this.usedCount++;
        if (this.usedCount >= this.maxUses) {
            this.status = "EXHAUSTED";
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
