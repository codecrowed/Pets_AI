package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pet_ai_agent_config")
public class PetAiAgentConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "agent_client_id", nullable = false)
    private Long agentClientId;

    @Column(name = "agent_name", nullable = false)
    private String agentName;

    @Column(name = "agent_desc", nullable = false, columnDefinition = "text")
    private String agentDesc;

    @Column(name = "agent_prompt_id", nullable = false)
    private Long agentPromptId;

    @Column(name = "slot", nullable = false, columnDefinition = "text")
    private String slot;

    @Column(name = "status", nullable = false)
    private Short status;

    @Column(name = "delete_flag", nullable = false)
    private Short deleteFlag;

    public Long getAgentId() {
        return agentId;
    }

    public Long getAgentClientId() {
        return agentClientId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getAgentDesc() {
        return agentDesc;
    }

    public Long getAgentPromptId() {
        return agentPromptId;
    }

    public String getSlot() {
        return slot;
    }
}
