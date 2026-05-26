package jiangxiaopeng.ai.ai.infrastructure.springai;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jiangxiaopeng.ai.ai.domain.service.ChatCompletionService;
import jiangxiaopeng.ai.ai.infrastructure.agent.MultiAgentChatOrchestrator;
import reactor.core.publisher.Flux;

/**
 * 多 Agent：主 Agent 通过工具 {@code dispatch_sub_agent}（{@link jiangxiaopeng.ai.ai.infrastructure.tool.SubAgentDispatchTool}）派发子任务；
 * 子 Agent 在工具内执行；会话记忆由 {@link jiangxiaopeng.ai.ai.infrastructure.agent.advisor.DomainMessageChatMemoryAdvisor} 处理。
 */
@Service
public class SpringAiChatService implements ChatCompletionService {

    private final MultiAgentChatOrchestrator multiAgentChatOrchestrator;

    public SpringAiChatService(MultiAgentChatOrchestrator multiAgentChatOrchestrator) {
        this.multiAgentChatOrchestrator = multiAgentChatOrchestrator;
    }

    @Override
    public String chat(String conversationId, String userMessage) {
        return multiAgentChatOrchestrator.completeWithMemory(conversationId, userMessage);
    }

    @Override
    public Flux<ChatResponse> streamChat(String conversationId, String userMessage) {
        return multiAgentChatOrchestrator.streamWithMemory(conversationId, userMessage);
    }

    @Override
    public String streamChat(String conversationId, String userMessage, ResponseBodyEmitter emitter) {
        return multiAgentChatOrchestrator.streamWithMemory(conversationId, userMessage, emitter);
    }
}
