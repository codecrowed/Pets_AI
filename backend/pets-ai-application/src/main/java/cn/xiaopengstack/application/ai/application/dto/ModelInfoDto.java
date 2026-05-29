package jiangxiaopeng.ai.ai.application.dto;

public record ModelInfoDto(
        String id,
        String name,
        String provider,
        boolean supportsStreaming,
        boolean supportsVision,
        int maxTokens,
        String requiredPlan
) {}
