package jiangxiaopeng.ai.conversation.domain.model;

import jiangxiaopeng.ai.shared.domain.vo.UserId;

public record MessageFeedback(UserId userId, FeedbackType type) {

    public enum FeedbackType {
        LIKE, DISLIKE
    }
}
