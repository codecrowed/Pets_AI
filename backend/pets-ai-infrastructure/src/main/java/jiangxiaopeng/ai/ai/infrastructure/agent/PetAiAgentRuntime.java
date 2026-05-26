package jiangxiaopeng.ai.ai.infrastructure.agent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * 单个 Agent 一次对话所需的装配结果：来自库的 {@link ChatModel}、处理后的系统提示词、Method Tool 实例。
 */
public record PetAiAgentRuntime(
        long agentId,
        long clientConfigId,
        ChatModel chatModel,
        Prompt processedSystemPrompt,
        List<ToolCallback> toolCallbacks
) {}
