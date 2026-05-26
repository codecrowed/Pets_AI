package jiangxiaopeng.ai.ai.infrastructure.agent;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jiangxiaopeng.ai.ai.infrastructure.tool.SubAgentDispatchTool;
import jiangxiaopeng.ai.shared.utils.SpringContextUtil;

import java.util.Optional;

/**
 * 将 {@code pet_ai_tool_config.tool_name} 与 Spring AI Method Tool Bean 关联（与 &#64;Tool name 一致）。
 */
@Component
public class PetAiMethodToolRegistry {

    /**
     * @param toolName {@link jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiToolConfigEntity#getToolName}
     */
    public Optional<Object> resolveMethodToolBean(String toolName) {
        if (toolName == null || toolName.isBlank()) {
            return Optional.empty();
        }
       
        return Optional.ofNullable(SpringContextUtil.getBean(toolName));
    }
}
