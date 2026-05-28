package jiangxiaopeng.ai.conversation.application.dto;

import java.time.Instant;

public record ChatSummaryDto(
        String chatId,
        Long uid,
        String title,
        Instant updatedAt
) {}
