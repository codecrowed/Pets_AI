package jiangxiaopeng.ai.ai.infrastructure.tool;

import java.util.Map;
import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jiangxiaopeng.ai.ai.infrastructure.agent.AgentPromptContext;
import jiangxiaopeng.ai.ai.infrastructure.agent.AgentToolContext;
import jiangxiaopeng.ai.ai.infrastructure.agent.PetAiAgentRuntime;
import jiangxiaopeng.ai.ai.infrastructure.agent.PetAiAgentRuntimeAssembler;
import jiangxiaopeng.ai.ai.infrastructure.agent.PetAiClientAssemblyService;
import jiangxiaopeng.ai.ai.infrastructure.sse.SseEmitterHelper;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

/**
 * 主 Agent 可调用的 Method Tool：在工具方法内按库表装配子 Agent 并执行（无会话记忆）。
 */
@Component("distribute_agent_task")
@Log4j2
public class SubAgentDispatchTool {

    private final PetAiAgentRuntimeAssembler agentRuntimeAssembler;
    private final PetAiClientAssemblyService clientAssembly;
    private final SseEmitterHelper sseEmitterHelper;

    public SubAgentDispatchTool(
            @Lazy PetAiAgentRuntimeAssembler agentRuntimeAssembler,
            PetAiClientAssemblyService clientAssembly,
            SseEmitterHelper sseEmitterHelper) {
        this.agentRuntimeAssembler = agentRuntimeAssembler;
        this.clientAssembly = clientAssembly;
        this.sseEmitterHelper = sseEmitterHelper;
    }

    @Tool(
            name = "distribute_agent_task",
            description = "将子任务派发给指定子 Agent 执行并返回其文本结果。主 Agent 在需要领域专家、与主链路不同的能力时调用；调用前给出清晰的任务说明。",
            returnDirect = true
    )
    public String dispatchSubAgent(
            SubAgentDispatchParam subAgentDispatchParam,
            ToolContext toolContext) {
        log.info("主Agent开始为子Agent分配任务: {}", subAgentDispatchParam);
        long targetAgentId = subAgentDispatchParam.targetAgentId();
        String userInstant = subAgentDispatchParam.userInstant();
        String userMessage = subAgentDispatchParam.userMessage();
        String slotJson = subAgentDispatchParam.slotJson();

        Map<String, Object> toolCtx = toolContext != null ? toolContext.getContext() : Map.of();
        AgentToolContext agentToolContext = (AgentToolContext) toolCtx.get(AgentToolContext.CONTEXT_KEY);
        
        String conversationId = agentToolContext != null ? agentToolContext.conversationId() : null;
        ResponseBodyEmitter emitter = agentToolContext != null ? agentToolContext.emitter() : null;

        AgentPromptContext context = AgentPromptContext.withToolExecute(
            targetAgentId + "_" + conversationId, 
            targetAgentId, 
            userMessage
        );

        PetAiAgentRuntime runtime = agentRuntimeAssembler.assembleSubAgent(context);
        ChatClient client = clientAssembly.createStatelessChatClient(runtime.chatModel(), runtime.toolCallbacks());

        String userPrompt = buildUserPrompt(userInstant, userMessage, slotJson);

        if (Objects.nonNull(emitter)) {
            return executeStreamingMode(client, runtime, userPrompt, toolContext, agentToolContext);
        } else {
            return executeSyncMode(client, runtime, userPrompt, toolContext);
        }
    }

    private String executeStreamingMode(
            ChatClient client, 
            PetAiAgentRuntime runtime, 
            String userPrompt, 
            ToolContext toolContext,
            AgentToolContext agentToolContext) {

        String messageId = agentToolContext != null ? agentToolContext.messageId() : null;
        ResponseBodyEmitter emitter = agentToolContext != null ? agentToolContext.emitter() : null;
        
        Flux<String> contentFlux = client.prompt(runtime.processedSystemPrompt())
                .user(userPrompt)
                .toolContext(Map.of(AgentToolContext.CONTEXT_KEY, agentToolContext))
                .stream()
                .chatResponse()
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        return response.getResult().getOutput().getText();
                    }
                    return null;
                })
                .filter(Objects::nonNull);

        return sseEmitterHelper.streamToEmitterBlocking(
                emitter,
                contentFlux,
                messageId,
                () -> log.debug("SubAgent streaming completed"),
                e -> log.error("SubAgent streaming error: {}", e.getMessage())
        );
    }

    private String executeSyncMode(
            ChatClient client, 
            PetAiAgentRuntime runtime, 
            String userPrompt, 
            ToolContext toolContext) {
        return client.prompt(runtime.processedSystemPrompt())
                .user(userPrompt)
                .toolContext(toolContext.getContext())
                .call()
                .content();
    }

    private String buildUserPrompt(String userInstant, String userMessage, String slotJson) {
        return "用户意图：" + userInstant + "\n用户原始消息：" + userMessage + "\n槽位信息：" + slotJson;
    }

    public record SubAgentDispatchParam(
        @ToolParam(description = "子 Agent 的业务 agent_id") long targetAgentId, 
        @ToolParam(description = "用户意图") String userInstant, 
        @ToolParam(description = "用户原始消息") String userMessage, 
        @ToolParam(description = "槽位信息") String slotJson) {}
}
