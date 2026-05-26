package jiangxiaopeng.ai.conversation.application.dto;

import java.time.Instant;
import java.util.List;

public record ChatListResponse(
        List<ChatGroup> groups,
        PageInfo pageInfo
) {
    public record ChatGroup(String dateLabel, List<ChatSummaryDto> chats) {}
}
