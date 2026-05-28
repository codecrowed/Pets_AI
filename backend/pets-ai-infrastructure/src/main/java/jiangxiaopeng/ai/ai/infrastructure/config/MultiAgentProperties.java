package jiangxiaopeng.ai.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.multi-agent")
public class MultiAgentProperties {

    /**
     * 关闭时走单轮 ChatClient（仅主会话记忆），与旧行为一致。
     */
    private boolean enabled = true;

    /**
     * 主 Agent 的业务 agent_id（对应 pet_ai_agent_config.agent_id）。
     */
    private long mainAgentId = 1L;

    /**
     * true：忽略库表，始终使用内置模拟配置（表无数据或联调时可与自动检测二选一）。
     */
    private boolean forceSimulation = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getMainAgentId() {
        return mainAgentId;
    }

    public void setMainAgentId(long mainAgentId) {
        this.mainAgentId = mainAgentId;
    }

    public boolean isForceSimulation() {
        return forceSimulation;
    }

    public void setForceSimulation(boolean forceSimulation) {
        this.forceSimulation = forceSimulation;
    }
}
