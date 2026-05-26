package jiangxiaopeng.ai.conversation.infrastructure.ai;

import jiangxiaopeng.ai.ai.domain.service.ChatCompletionService;
import jiangxiaopeng.ai.conversation.domain.service.AiModelRouter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import reactor.core.publisher.Flux;

import java.util.Objects;

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
    public Flux<String> streamComplete(String conversationId, String userMessage) {
        return chatService.streamChat(conversationId, userMessage)
                .map(response -> response.getResult().getOutput().getText())
                .filter(Objects::nonNull);
    }

    @Override
    public String streamComplete(String conversationId, String userMessage, ResponseBodyEmitter emitter) {
        return chatService.streamChat(conversationId, userMessage, emitter);
    }
}
