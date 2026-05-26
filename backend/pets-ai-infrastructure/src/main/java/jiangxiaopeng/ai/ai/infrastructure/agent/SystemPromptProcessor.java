package jiangxiaopeng.ai.ai.infrastructure.agent;

import org.springframework.ai.chat.prompt.Prompt;

/**
 * 处理 {@code pet_ai_prompt_config.prompt_value} 中的占位符（如 {@code {{agentName}}}、{@code {{slot.xxx}}}）。
 */
public interface SystemPromptProcessor {

    Prompt process(String template, AgentPromptContext context);
}
