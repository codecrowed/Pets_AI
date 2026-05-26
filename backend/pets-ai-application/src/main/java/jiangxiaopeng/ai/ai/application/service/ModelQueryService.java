package jiangxiaopeng.ai.ai.application.service;

import jiangxiaopeng.ai.ai.application.dto.ModelInfoDto;
import jiangxiaopeng.ai.ai.domain.service.ModelRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelQueryService {

    private final ModelRegistry modelRegistry;

    public ModelQueryService(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    public List<ModelInfoDto> getAvailableModels() {
        return modelRegistry.getAvailableModels().stream()
                .map(m -> new ModelInfoDto(
                        m.id(), m.name(), m.provider().name().toLowerCase(),
                        m.supportsStreaming(), m.supportsVision(),
                        m.maxTokens(), m.requiredPlan()
                ))
                .toList();
    }
}
