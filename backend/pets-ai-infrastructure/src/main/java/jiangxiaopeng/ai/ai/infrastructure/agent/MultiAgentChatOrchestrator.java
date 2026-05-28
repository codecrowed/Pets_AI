package jiangxiaopeng.ai.ai.infrastructure.agent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jiangxiaopeng.ai.ai.infrastructure.agent.advisor.DomainMessageChatMemoryAdvisor;
import jiangxiaopeng.ai.ai.infrastructure.config.AiConfig;
import jiangxiaopeng.ai.ai.infrastructure.sse.SseEmitterHelper;
import jiangxiaopeng.ai.ai.infrastructure.tool.SubAgentDispatchTool;
import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.model.MessageRole;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

/**
 * 多 Agent：主 Agent 侧使用库表装配的 system 与 Method Tool；子 Agent 在 {@link SubAgentDispatchTool} 内执行。
 * emitter 通过 AgentToolContext 传递给 SubAgentDispatchTool。
 */
@Service
@Log4j2
public class MultiAgentChatOrchestrator {

    private final PetAiAgentRuntimeAssembler agentRuntimeAssembler;
    private final MessageRepository messageRepository;

    public MultiAgentChatOrchestrator(PetAiAgentRuntimeAssembler agentRuntimeAssembler,
                                      MessageRepository messageRepository) {
        this.agentRuntimeAssembler = agentRuntimeAssembler;
        this.messageRepository = messageRepository;
    }

    public String completeWithMemory(String conversationId, String userMessage) {
        AgentPromptContext ctx = AgentPromptContext.forMainTurn(conversationId, userMessage);
        PetAiAgentRuntime main = agentRuntimeAssembler.assembleMainAgent(ctx);

        DomainMessageChatMemoryAdvisor memoryAdvisor = DomainMessageChatMemoryAdvisor
                .builder(messageRepository)
                .maxMessages(AiConfig.MAX_CONTEXT_MESSAGES)
                .conversationId(conversationId)
                .agentId(String.valueOf(main.agentId()))
                .build();

        AgentToolContext toolContext = AgentToolContext.ofSync(conversationId);

        return ChatClient.builder(main.chatModel()).build()
                        .prompt(main.processedSystemPrompt())
                        .toolCallbacks(main.toolCallbacks())
                        .toolContext(toolContext.toMap())
                        .user(userMessage)
                        .advisors(memoryAdvisor)
                        .call()
                        .content();
    }

    public Flux<ChatResponse> streamWithMemory(String conversationId, String userMessage) {
        AgentPromptContext ctx = AgentPromptContext.forMainTurn(conversationId, userMessage);
        PetAiAgentRuntime main = agentRuntimeAssembler.assembleMainAgent(ctx);

        DomainMessageChatMemoryAdvisor memoryAdvisor = DomainMessageChatMemoryAdvisor
                .builder(messageRepository)
                .maxMessages(AiConfig.MAX_CONTEXT_MESSAGES)
                .conversationId(conversationId)
                .agentId(String.valueOf(main.agentId()))
                .build();

        AgentToolContext toolContext = AgentToolContext.ofSync(conversationId);

        return ChatClient.builder(main.chatModel()).build()
                        .prompt(main.processedSystemPrompt())
                        .toolCallbacks(main.toolCallbacks())
                        .toolContext(toolContext.toMap())
                        .user(userMessage)
                        .advisors(memoryAdvisor)
                        .stream()
                        .chatResponse();
    }

    /**
     * 流式调用主 Agent，将响应流式输出到 emitter。
     * 主 Agent 的回复将通过 SSE 流式推送给客户端。
     */
    public String streamWithMemory(String conversationId, String userMessage, ResponseBodyEmitter emitter) {
        AgentPromptContext ctx = AgentPromptContext.forMainTurn(conversationId, userMessage);
        PetAiAgentRuntime main = agentRuntimeAssembler.assembleMainAgent(ctx);

        DomainMessageChatMemoryAdvisor memoryAdvisor = DomainMessageChatMemoryAdvisor
                .builder(messageRepository)
                .maxMessages(AiConfig.MAX_CONTEXT_MESSAGES)
                .conversationId(conversationId)
                .agentId(String.valueOf(main.agentId()))
                .build();

        String messageId = UUID.randomUUID().toString();
        AgentToolContext toolContext = AgentToolContext.ofStreaming(conversationId, messageId, emitter);

        String result = ChatClient.builder(main.chatModel()).build()
                        .prompt(main.processedSystemPrompt())
                        .toolCallbacks(main.toolCallbacks())
                        .toolContext(Map.of(AgentToolContext.CONTEXT_KEY, toolContext))
                        .user(userMessage)
                        .advisors(memoryAdvisor)
                        .call()
                        .content();
        return result;
    }

}
