package jiangxiaopeng.ai.conversation.domain.model;

import jiangxiaopeng.ai.shared.domain.vo.Uid;
import jiangxiaopeng.ai.shared.domain.vo.UserId;

import java.time.Instant;

public class Message {

    private Long id;
    private Uid uid;
    private Long sessionId;
    private MessageRole role;
    private String content;
    private String model;
    private TokenUsage tokenUsage;
    private MessageFeedback feedback;
    private MessageStatus status;
    private Instant createdAt;

    public Message() {}

    public static Message createUserMessage(Long sessionId, String content) {
        Message msg = new Message();
        msg.uid = Uid.generate();
        msg.sessionId = sessionId;
        msg.role = MessageRole.USER;
        msg.content = content;
        msg.status = MessageStatus.COMPLETED;
        msg.createdAt = Instant.now();
        return msg;
    }

    public static Message createPendingAiMessage(Long sessionId, String model) {
        Message msg = new Message();
        msg.uid = Uid.generate();
        msg.sessionId = sessionId;
        msg.role = MessageRole.ASSISTANT;
        msg.content = "";
        msg.model = model;
        msg.status = MessageStatus.STREAMING;
        msg.createdAt = Instant.now();
        return msg;
    }

    public void complete(String fullContent) {
        this.content = fullContent;
        this.status = MessageStatus.COMPLETED;
    }

    public void fail() {
        this.status = MessageStatus.FAILED;
    }

    public void setTokenUsage(TokenUsage usage) {
        this.tokenUsage = usage;
    }

    public void submitFeedback(UserId userId, String type) {
        this.feedback = new MessageFeedback(userId, MessageFeedback.FeedbackType.valueOf(type));
    }

    public void clearFeedback() {
        this.feedback = null;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Uid getUid() { return uid; }
    public void setUid(Uid uid) { this.uid = uid; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public MessageRole getRole() { return role; }
    public void setRole(MessageRole role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public TokenUsage getTokenUsage() { return tokenUsage; }
    public MessageFeedback getFeedback() { return feedback; }
    public void setFeedback(MessageFeedback feedback) { this.feedback = feedback; }
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
