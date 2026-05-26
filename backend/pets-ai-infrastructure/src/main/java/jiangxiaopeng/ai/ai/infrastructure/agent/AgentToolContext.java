package jiangxiaopeng.ai.ai.infrastructure.agent;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 工具执行上下文，封装 Spring AI ToolContext 中传递的所有参数。
 * 提供类型安全的访问方式，避免直接操作 Map 的字符串键。
 */
public record AgentToolContext(
        String conversationId,
        String messageId,
        ResponseBodyEmitter emitter
) {

    public static final String CONTEXT_KEY = "agentToolContext";

    public static AgentToolContext of(String conversationId, String messageId, ResponseBodyEmitter emitter) {
        return new AgentToolContext(conversationId, messageId, emitter);
    }

    public static AgentToolContext ofSync(String conversationId) {
        return new AgentToolContext(conversationId, null, null);
    }

    public static AgentToolContext ofStreaming(String conversationId, String messageId, ResponseBodyEmitter emitter) {
        return new AgentToolContext(conversationId, messageId, emitter);
    }

    /**
     * 将 AgentToolContext 转换为 Map，用于传入 Spring AI 的 toolContext。
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(CONTEXT_KEY, this);
        if (conversationId != null) {
            map.put("conversationId", conversationId);
        }
        if (messageId != null) {
            map.put("messageId", messageId);
        }
        if (emitter != null) {
            map.put("emitter", emitter);
        }
        return map;
    }

    /**
     * 从 Spring AI ToolContext 的 Map 中提取 AgentToolContext。
     */
    public static AgentToolContext fromMap(Map<String, Object> contextMap) {
        if (contextMap == null) {
            return null;
        }
        
        Object ctx = contextMap.get(CONTEXT_KEY);
        if (ctx instanceof AgentToolContext agentToolContext) {
            return agentToolContext;
        }
        
        String conversationId = (String) contextMap.get("conversationId");
        String messageId = (String) contextMap.get("messageId");
        ResponseBodyEmitter emitter = (ResponseBodyEmitter) contextMap.get("emitter");
        
        return new AgentToolContext(conversationId, messageId, emitter);
    }

    public boolean isStreaming() {
        return emitter != null;
    }
}
