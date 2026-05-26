package jiangxiaopeng.ai.conversation.domain.model;

public record TokenUsage(int promptTokens, int completionTokens) {
    public int totalTokens() {
        return promptTokens + completionTokens;
    }
}
