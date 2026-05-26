package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pet_ai_prompt_config")
public class PetAiPromptConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prompt_id", nullable = false)
    private Long promptId;

    @Column(name = "prompt_desc", nullable = false)
    private String promptDesc;

    @Column(name = "prompt_value", nullable = false, columnDefinition = "text")
    private String promptValue;

    @Column(name = "prompt_type", nullable = false)
    private String promptType;

    @Column(name = "status", nullable = false)
    private Short status;

    @Column(name = "delete_flag", nullable = false)
    private Short deleteFlag;

    public Long getPromptId() {
        return promptId;
    }

    public String getPromptValue() {
        return promptValue;
    }

    public String getPromptType() {
        return promptType;
    }

    public Short getStatus() {
        return status;
    }

    public Short getDeleteFlag() {
        return deleteFlag;
    }
}
