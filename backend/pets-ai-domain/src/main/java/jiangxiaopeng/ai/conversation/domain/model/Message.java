package jiangxiaopeng.ai.conversation.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private Long id;
    private Long uid;
    private String msgId;
    private Long sessionId;
    private MessageRole role;
    private String content;
    private String model;
    private TokenUsage tokenUsage;
    private MessageFeedback feedback;
    private MessageStatus status;
    private Instant createdAt;
    private String agentId;


    public static Message createUserMessage(
        Long uid, 
        Long sessionId, 
        String agentId,
        String content) {
        Message msg = new Message();
        msg.uid = uid;
        msg.sessionId = sessionId;
        msg.role = MessageRole.USER;
        msg.content = content;
        msg.status = MessageStatus.COMPLETED;
        msg.createdAt = Instant.now();
        msg.agentId = agentId;
        return msg;
    }

    public static Message createPendingAiMessage(
        Long uid, 
        Long sessionId, 
        String model,
        String agentId
    ) {
        Message msg = new Message();
        msg.uid = uid;
        msg.sessionId = sessionId;
        msg.role = MessageRole.ASSISTANT;
        msg.content = "";
        msg.model = model;
        msg.status = MessageStatus.STREAMING;
        msg.createdAt = Instant.now();
        msg.agentId = agentId;
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

    public void submitFeedback(Long uid, String type) {
        this.feedback = new MessageFeedback(uid, MessageFeedback.FeedbackType.valueOf(type));
    }

    public void clearFeedback() {
        this.feedback = null;
    }

}
