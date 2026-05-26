package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pet_ai_tool_config")
public class PetAiToolConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_id", nullable = false)
    private Long toolId;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(name = "tool_type", nullable = false)
    private String toolType;

    @Column(name = "return_direct", nullable = false, length = 1)
    private String returnDirect;

    @Column(name = "prompt_id")
    private Long promptId;

    @Column(name = "status", nullable = false)
    private Short status;

    @Column(name = "delete_flag", nullable = false)
    private Short deleteFlag;

    public Long getToolId() {
        return toolId;
    }

    public String getToolName() {
        return toolName;
    }

    public String getToolType() {
        return toolType;
    }
}
