package jiangxiaopeng.ai.conversation.domain.service;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import reactor.core.publisher.Flux;

public interface AiModelRouter {
    String complete(String conversationId, String userMessage);
    Flux<String> streamComplete(String conversationId, String userMessage);
    String streamComplete(String conversationId, String userMessage, ResponseBodyEmitter emitter);
}
