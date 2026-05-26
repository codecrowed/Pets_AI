package jiangxiaopeng.ai.ai.domain.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import reactor.core.publisher.Flux;

public interface ChatCompletionService {
    String chat(String conversationId, String userMessage);
    Flux<ChatResponse> streamChat(String conversationId, String userMessage);
    String streamChat(String conversationId, String userMessage, ResponseBodyEmitter emitter);
}
