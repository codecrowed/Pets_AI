package jiangxiaopeng.ai.ai.infrastructure.agent;

import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentConfigEntity;

/**
 * 系统提示词占位符替换上下文：预置变量 + slot JSON 中的键。
 */
public record AgentPromptContext(
        String conversationId,
        String userMessage,
        Long agentId,
        String agentName,
        String agentDesc
) {

    public static AgentPromptContext forMainTurn(String conversationId, String userMessage) {
        return new AgentPromptContext(conversationId, userMessage, null, null, null);
    }


    public AgentPromptContext withAgent(PetAiAgentConfigEntity agent) {
        return new AgentPromptContext(
                conversationId,
                userMessage,
                agent.getAgentId(),
                agent.getAgentName(),
                agent.getAgentDesc());
    }

    /** 指定当前装配目标 Agent（主/子）的业务 agent_id。 */
    public AgentPromptContext withAgentId(Long agentId) {
        return new AgentPromptContext(
                conversationId,
                userMessage,
                agentId,
                agentName,
                agentDesc);
    }

    /** 指定当前装配目标 Agent（主/子）的业务 agent_id。 */
    public static AgentPromptContext withToolExecute(String conversationId, Long agentId, String userMessage) {
        return new AgentPromptContext(
                conversationId,
                userMessage,
                agentId,
                null,
                null);
    }
}
