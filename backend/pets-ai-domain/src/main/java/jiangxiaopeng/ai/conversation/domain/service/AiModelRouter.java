package jiangxiaopeng.ai.conversation.domain.service;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface AiModelRouter {
    /**
     * 阻塞式输出
     * @param conversationId
     * @param userMessage
     * @return
     */
    String complete(String conversationId, String userMessage);
    /**
     * 使用SSE流式输出
     * @param conversationId
     * @param userMessage
     * @param emitter
     * @return
     */
    String streamComplete(String conversationId, String userMessage, ResponseBodyEmitter emitter);
}
