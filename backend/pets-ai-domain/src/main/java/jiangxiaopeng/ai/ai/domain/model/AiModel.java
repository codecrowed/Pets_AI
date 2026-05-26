package jiangxiaopeng.ai.ai.domain.model;

public record AiModel(
        String id,
        String name,
        AiProvider provider,
        boolean supportsStreaming,
        boolean supportsVision,
        int maxTokens,
        String requiredPlan
) {}
