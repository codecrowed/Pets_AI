package jiangxiaopeng.ai.ai.domain.service;

import jiangxiaopeng.ai.ai.domain.model.AiModel;

import java.util.List;

public interface ModelRegistry {
    List<AiModel> getAvailableModels();
    AiModel getModel(String modelId);
}
