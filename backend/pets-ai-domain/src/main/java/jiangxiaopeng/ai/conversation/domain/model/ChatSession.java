package jiangxiaopeng.ai.conversation.domain.model;

import java.time.Instant;
import java.util.UUID;

import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSession {

    private Long id;
    private String chatId;
    private Long uid;
    private String title;
    private String model;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ChatSession() {}

    public static ChatSession create(Long uid, String title, String model) {
        ChatSession session = new ChatSession();
        session.chatId = UUID.randomUUID().toString();
        session.uid = uid;
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

    public void validateOwnership(Long uid) {
        if (!this.uid.equals(uid)) {
            throw new BusinessException(ErrorCode.CHAT_002);
        }
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

}
