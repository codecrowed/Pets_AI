package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pet_ai_skill_config")
public class PetAiSkillConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "skill_desc", nullable = false, columnDefinition = "text")
    private String skillDesc;

    @Column(name = "prompt_id")
    private Long promptId;

    @Column(name = "status", nullable = false)
    private Short status;

    @Column(name = "delete_flag", nullable = false)
    private Short deleteFlag;

    public Long getSkillId() {
        return skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public Long getPromptId() {
        return promptId;
    }

    public String getSkillDesc() {
        return skillDesc;
    }
}
