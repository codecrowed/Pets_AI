package jiangxiaopeng.ai.ai.infrastructure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jiangxiaopeng.ai.ai.infrastructure.agent.PetAiAgentRuntimeAssembler;
import jiangxiaopeng.ai.ai.infrastructure.agent.PetAiClientAssemblyService;
import jiangxiaopeng.ai.ai.infrastructure.agent.advisor.DomainMessageChatMemoryAdvisor;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentConfigRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiClientConfigRepository;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;

/**
 * 主会话 {@link ChatModel} 优先由 {@code pet_ai_agent_config} + {@code pet_ai_client_config} 装配；
 * 无数据时回退到 spring.ai.openai.*。
 */
@Configuration
@EnableConfigurationProperties(MultiAgentProperties.class)
public class AiConfig {
    public static final int MAX_CONTEXT_MESSAGES = 20;

    @Bean
    public ChatModel mainChatModel(
            PetAiAgentRuntimeAssembler runtimeAssembler,
            PetAiClientAssemblyService clientAssembly,
            PetAiAgentConfigRepository agentRepo,
            PetAiClientConfigRepository clientRepo,
            MultiAgentProperties multiAgentProperties,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.chat.options.model}") String model
    ) {
            return clientAssembly.buildChatModelFromYml(
                    baseUrl, 
                    apiKey,
                    "v1/chat/completions",
                    "v1/embeddings", 
                     model);
    }

    /**
     * 默认的 ChatClient，使用 DomainMessageChatMemoryAdvisor 直接操作领域 MessageRepository。
     */
    @Bean
    public ChatClient mainChatClient(
            ChatModel mainChatModel,
            MessageRepository messageRepository,
            PetAiAgentRuntimeAssembler runtimeAssembler) {
        DomainMessageChatMemoryAdvisor memoryAdvisor = DomainMessageChatMemoryAdvisor
                .builder(messageRepository)
                .maxMessages(MAX_CONTEXT_MESSAGES)
                .build();

        return ChatClient.builder(mainChatModel)
                .defaultAdvisors(memoryAdvisor)
                .build();
    }
}
