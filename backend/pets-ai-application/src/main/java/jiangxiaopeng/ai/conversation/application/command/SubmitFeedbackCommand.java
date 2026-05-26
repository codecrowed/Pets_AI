package jiangxiaopeng.ai.conversation.application.command;

public record SubmitFeedbackCommand(String messageId, Long userId, String type) {}
