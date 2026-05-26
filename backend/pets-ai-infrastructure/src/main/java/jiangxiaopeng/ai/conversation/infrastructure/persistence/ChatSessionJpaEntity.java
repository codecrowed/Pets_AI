package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import jiangxiaopeng.ai.shared.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_sessions")
public class ChatSessionJpaEntity extends BaseEntity {

    @Column(name = "uid", nullable = false, unique = true)
    private String uid;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "status", nullable = false)
    private String status;

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
