package jiangxiaopeng.ai.conversation.application.command;

public record SubmitFeedbackCommand(String msgId, Long uid, String type) {}
