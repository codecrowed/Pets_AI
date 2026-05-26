package jiangxiaopeng.ai.ai.infrastructure.springai;

import jiangxiaopeng.ai.ai.domain.model.AiModel;
import jiangxiaopeng.ai.ai.domain.model.AiProvider;
import jiangxiaopeng.ai.ai.domain.service.ModelRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringAiModelRegistry implements ModelRegistry {

    private static final List<AiModel> MODELS = List.of(
            new AiModel("deepseek", "deepseek", AiProvider.OPENAI, true, true, 4096, "FREE"),
            new AiModel("deepseek-mini", "deepseek Mini", AiProvider.OPENAI, true, true, 4096, "FREE"),
            new AiModel("gpt-4-turbo", "GPT-4 Turbo", AiProvider.OPENAI, true, true, 4096, "PRO"),
            new AiModel("claude-sonnet-4-20250514", "Claude Sonnet 4", AiProvider.ANTHROPIC, true, true, 4096, "FREE"),
            new AiModel("claude-opus-4-20250514", "Claude Opus 4", AiProvider.ANTHROPIC, true, true, 4096, "PRO")
    );

    @Override
    public List<AiModel> getAvailableModels() {
        return MODELS;
    }

    @Override
    public AiModel getModel(String modelId) {
        return MODELS.stream()
                .filter(m -> m.id().equals(modelId))
                .findFirst()
                .orElse(MODELS.get(0));
    }
}
