package jiangxiaopeng.ai.conversation.domain.model;

public record MessageFeedback(Long uid, FeedbackType type) {

    public enum FeedbackType {
        LIKE, DISLIKE
    }
}
