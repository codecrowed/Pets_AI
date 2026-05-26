package jiangxiaopeng.ai.conversation.application.dto;

import java.time.Instant;

public record ChatSummaryDto(
        String id,
        String title,
        Instant updatedAt
) {}
