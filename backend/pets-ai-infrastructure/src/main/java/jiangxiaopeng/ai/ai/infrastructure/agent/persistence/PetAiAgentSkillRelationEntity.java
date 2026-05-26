package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pet_ai_agent_skill_relation")
public class PetAiAgentSkillRelationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @Column(name = "status", nullable = false)
    private Short status;

    @Column(name = "delete_flag", nullable = false)
    private Short deleteFlag;

    public Long getAgentId() {
        return agentId;
    }

    public Long getSkillId() {
        return skillId;
    }
}
