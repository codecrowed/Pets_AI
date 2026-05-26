package jiangxiaopeng.ai.ai.infrastructure.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiPromptConfigEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiPromptConfigRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiSkillConfigEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiSkillConfigRepository;

@Component("get_skill_complete_info")
@Service
public class CompleteSkillInfoTool {

    private final PetAiSkillConfigRepository skillConfigRepository;

    private final PetAiPromptConfigRepository promptConfigRepository;

    public CompleteSkillInfoTool(PetAiSkillConfigRepository skillConfigRepository, PetAiPromptConfigRepository promptConfigRepository) {
        this.skillConfigRepository = skillConfigRepository;
        this.promptConfigRepository = promptConfigRepository;
    }

    @Tool(
        name = "get_skill_complete_info",
        description = "根据技能名称和技能ID，获取技能的完整信息"
    )
    public String completeSkillInfo(CompleteSkillInfoParam completeSkillInfoParam, ToolContext toolContext) {

        Long skillId = completeSkillInfoParam.skillId();
        PetAiSkillConfigEntity skillConfigEntity = skillConfigRepository.findBySkillIdAndStatus(skillId, (short) 1)
        .orElseThrow(() -> new IllegalStateException("Skill not found: " + skillId));
        
        Long promptId = skillConfigEntity.getPromptId();
        PetAiPromptConfigEntity promptConfigEntity = promptConfigRepository.findByPromptIdAndStatus(promptId, (short) 1)
        .orElseThrow(() -> new IllegalStateException("Prompt not found: " + promptId));
        
        return "完整技能信息：" + promptConfigEntity.getPromptValue();
    }


    public record CompleteSkillInfoParam(
        @ToolParam(description = "技能 ID") Long skillId,
        @ToolParam(description = "技能名称") String skillName
    ) {
    }
}
