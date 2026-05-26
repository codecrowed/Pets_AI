package jiangxiaopeng.ai.ai.infrastructure.agent.advisor;

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.model.TokenUsage;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;

/**
 * 自定义 ChatMemoryAdvisor，直接使用领域 {@link MessageRepository} 进行消息持久化，
 * 绕过 Spring AI 的 ChatMemory / ChatMemoryRepository 层，
 * 保存完整的数据库实体字段（model、tokenUsage、status 等）。
 *
 * <p>替代 {@code MessageChatMemoryAdvisor}，在 before 阶段加载历史消息并保存用户消息，
 * 在 after 阶段保存 AI 回复消息（含 model、token 用量等元数据）。
 */
public final class DomainMessageChatMemoryAdvisor implements BaseChatMemoryAdvisor {

    private static final Logger log = LoggerFactory.getLogger(DomainMessageChatMemoryAdvisor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MessageRepository messageRepository;

    private final int maxMessages;

    private final String defaultConversationId;

    private final int order;

    private final Scheduler scheduler;

    private final String agentId;

    private DomainMessageChatMemoryAdvisor(MessageRepository messageRepository, int maxMessages,
            String defaultConversationId, int order, Scheduler scheduler, String agentId) {
        Assert.notNull(messageRepository, "messageRepository cannot be null");
        Assert.hasText(defaultConversationId, "defaultConversationId cannot be null or empty");
        Assert.notNull(scheduler, "scheduler cannot be null");
        this.messageRepository = messageRepository;
        this.maxMessages = maxMessages;
        this.defaultConversationId = defaultConversationId;
        this.order = order;
        this.scheduler = scheduler;
        this.agentId = agentId;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String conversationId = getConversationId(chatClientRequest.context(), this.defaultConversationId);
        Long sessionId = Long.parseLong(conversationId);

        // 1. 从数据库加载领域消息历史
        List<Message> domainMessages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        // 2. 消息窗口：只保留最近 maxMessages 条
        if (domainMessages.size() > maxMessages) {
            domainMessages = domainMessages.subList(domainMessages.size() - maxMessages, domainMessages.size());
        }

        // 3. 领域消息 → Spring AI 消息（供 Prompt 使用）
        List<org.springframework.ai.chat.messages.Message> memoryMessages = domainMessages.stream()
                .map(this::toSpringAiMessage)
                .toList();

        // 4. 合并：历史消息 + 当前请求指令（system prompt + user message）
        List<org.springframework.ai.chat.messages.Message> processedMessages = new ArrayList<>(memoryMessages);
        processedMessages.addAll(chatClientRequest.prompt().getInstructions());

        ChatClientRequest processedRequest = chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().mutate().messages(processedMessages).build())
                .build();

        // 5. 将本轮用户消息持久化为领域 Message
        UserMessage userMessage = processedRequest.prompt().getUserMessage();
        if (userMessage != null) {
            Message domainUserMsg = Message.createUserMessage(sessionId, userMessage.getText());
            messageRepository.save(domainUserMsg);
        }

        return processedRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        String conversationId = getConversationId(chatClientResponse.context(), this.defaultConversationId);
        Long sessionId = Long.parseLong(conversationId);

        if (chatClientResponse.chatResponse() == null) {
            return chatClientResponse;
        }

        var chatResponse = chatClientResponse.chatResponse();

        // 从 ChatResponse 元数据提取 model
        String model = null;
        if (chatResponse.getMetadata() != null) {
            model = chatResponse.getMetadata().getModel();
        }

        // 从 ChatResponse 元数据提取 token 用量
        TokenUsage tokenUsage = null;
        if (chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null) {
            var usage = chatResponse.getMetadata().getUsage();
            tokenUsage = new TokenUsage(
                    (int) usage.getPromptTokens(),
                    (int) usage.getCompletionTokens());
        }

        // 逐条保存 AI 回复为领域 Message，填充 model、tokenUsage、status 等完整字段
        for (var generation : chatResponse.getResults()) {
            String content = generation.getOutput().getText();
            log.debug("Original AI content (first 200 chars): {}", 
                    content != null && content.length() > 200 ? content.substring(0, 200) + "..." : content);
            String normalizedContent = normalizeContent(content);
            log.debug("Normalized AI content (first 200 chars): {}", 
                    normalizedContent != null && normalizedContent.length() > 200 ? normalizedContent.substring(0, 200) + "..." : normalizedContent);
            Message aiMsg = Message.createPendingAiMessage(sessionId, model);
            aiMsg.complete(normalizedContent);
            if (tokenUsage != null) {
                aiMsg.setTokenUsage(tokenUsage);
            }
            messageRepository.save(aiMsg);
        }

        return chatClientResponse;
    }

    /**
     * 规范化 AI 回复内容：检测并移除不必要的 JSON 字符串包装。
     * 当使用 returnDirect=true 的工具时，Spring AI 可能会将返回值作为 JSON 字符串处理，
     * 导致内容被双引号包裹且特殊字符被转义。
     * 支持递归处理多层 JSON 包装。
     */
    private String normalizeContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String result = content;
        int maxIterations = 5;
        int iteration = 0;
        
        while (iteration < maxIterations) {
            String trimmed = result.trim();
            
            // 检测是否是被双引号包裹的 JSON 字符串
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
                try {
                    String unescaped = objectMapper.readValue(trimmed, String.class);
                    log.debug("Normalized JSON-escaped content (iteration {}), original length: {}, normalized length: {}", 
                            iteration + 1, result.length(), unescaped.length());
                    
                    // 如果解析后内容相同，说明没有更多的转义需要处理
                    if (unescaped.equals(result)) {
                        break;
                    }
                    result = unescaped;
                    iteration++;
                } catch (Exception e) {
                    log.trace("Content is not a JSON-escaped string (iteration {}), keeping current: {}", 
                            iteration, e.getMessage());
                    break;
                }
            } else {
                break;
            }
        }
        
        if (iteration > 0) {
            log.info("Content was normalized after {} iteration(s), final length: {}", iteration, result.length());
        }
        
        return result;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
            StreamAdvisorChain streamAdvisorChain) {
        Scheduler streamScheduler = this.getScheduler();
        return Mono.just(chatClientRequest)
                .publishOn(streamScheduler)
                .map(request -> this.before(request, streamAdvisorChain))
                .flatMapMany(streamAdvisorChain::nextStream)
                .transform(flux -> new ChatClientMessageAggregator().aggregateChatClientResponse(flux,
                        response -> this.after(response, streamAdvisorChain)));
    }

    private org.springframework.ai.chat.messages.Message toSpringAiMessage(Message msg) {
        return switch (msg.getRole()) {
            case USER -> new UserMessage(msg.getContent());
            case ASSISTANT -> new AssistantMessage(msg.getContent());
            case SYSTEM -> new SystemMessage(msg.getContent());
        };
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static Builder builder(MessageRepository messageRepository) {
        return new Builder(messageRepository);
    }

    public static final class Builder {

        private final MessageRepository messageRepository;

        private int maxMessages = 20;

        private String conversationId = ChatMemory.DEFAULT_CONVERSATION_ID;

        private int order = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER;

        private Scheduler scheduler = BaseAdvisor.DEFAULT_SCHEDULER;

        private String agentId = null;

        private Builder(MessageRepository messageRepository) {
            this.messageRepository = messageRepository;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public DomainMessageChatMemoryAdvisor build() {
            return new DomainMessageChatMemoryAdvisor(
                    this.messageRepository, this.maxMessages, this.conversationId, this.order, this.scheduler, this.agentId);
        }
    }
}
