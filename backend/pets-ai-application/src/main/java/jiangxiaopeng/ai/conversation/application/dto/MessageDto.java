package jiangxiaopeng.ai.conversation.application.dto;

import java.time.Instant;
import java.util.List;

public record MessageDto(
        String id,
        String role,
        String content,
        CodeBlockData codeBlock,
        String feedbackType,
        String model,
        Instant createdAt
) {
    public record CodeBlockData(String language, String code) {}
}
