package jiangxiaopeng.ai.conversation.application.dto;

import java.util.List;

public record MessageListResponse(
        List<MessageDto> messages,
        String nextCursor,
        boolean hasMore
) {}
