package jiangxiaopeng.ai.conversation.domain.model;

import jiangxiaopeng.ai.shared.domain.vo.Uid;
import jiangxiaopeng.ai.shared.domain.vo.UserId;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;

import java.time.Instant;

public class ChatSession {

    private Long id;
    private Uid uid;
    private UserId userId;
    private String title;
    private String model;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ChatSession() {}

    public static ChatSession create(UserId userId, String title, String model) {
        ChatSession session = new ChatSession();
        session.uid = Uid.generate();
        session.userId = userId;
        session.title = title != null ? title : "New Chat";
        session.model = model != null ? model : "deepseek";
        session.status = "ACTIVE";
        session.createdAt = Instant.now();
        session.updatedAt = Instant.now();
        return session;
    }

    public void updateTitle(String newTitle) {
        if (newTitle != null && !newTitle.isBlank()) {
            this.title = newTitle;
            this.updatedAt = Instant.now();
        }
    }

    public void updateModel(String model) {
        if (model != null && !model.isBlank()) {
            this.model = model;
            this.updatedAt = Instant.now();
        }
    }

    public void softDelete() {
        this.status = "DELETED";
        this.updatedAt = Instant.now();
    }

    public void validateOwnership(UserId requestUserId) {
        if (!this.userId.equals(requestUserId)) {
            throw new BusinessException(ErrorCode.CHAT_002);
        }
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Uid getUid() { return uid; }
    public void setUid(Uid uid) { this.uid = uid; }
    public UserId getUserId() { return userId; }
    public void setUserId(UserId userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
