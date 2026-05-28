package jiangxiaopeng.ai.conversation.infrastructure.ai;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jiangxiaopeng.ai.ai.domain.service.ChatCompletionService;
import jiangxiaopeng.ai.conversation.domain.service.AiModelRouter;

@Component
public class AiModelRouterImpl implements AiModelRouter {

    private final ChatCompletionService chatService;

    public AiModelRouterImpl(ChatCompletionService chatService) {
        this.chatService = chatService;
    }

    @Override
    public String complete(String conversationId, String userMessage) {
        return chatService.chat(conversationId, userMessage);
    }

    @Override
    public String streamComplete(String conversationId, String userMessage, ResponseBodyEmitter emitter) {
        return chatService.streamChat(conversationId, userMessage, emitter);
    }
}
