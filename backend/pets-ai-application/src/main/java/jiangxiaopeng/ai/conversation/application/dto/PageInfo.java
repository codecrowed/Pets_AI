package jiangxiaopeng.ai.conversation.application.dto;

public record PageInfo(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {}
