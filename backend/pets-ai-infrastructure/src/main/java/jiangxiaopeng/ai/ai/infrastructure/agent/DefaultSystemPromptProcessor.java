package jiangxiaopeng.ai.ai.infrastructure.agent;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentSkillRelationEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentSkillRelationRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiSkillConfigEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiSkillConfigRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用占位符解析；
 */
@Component
@Slf4j
public class DefaultSystemPromptProcessor implements SystemPromptProcessor {

    private static final String SKILL_PLACEHOLDER = "skillList";
    private static final short ACTIVE = 1;

    private final PetAiAgentSkillRelationRepository agentSkillRelationRepo;

    private final PetAiSkillConfigRepository skillRepo;

    public DefaultSystemPromptProcessor(
        PetAiAgentSkillRelationRepository agentSkillRelationRepo, 
        PetAiSkillConfigRepository skillRepo) {
        this.agentSkillRelationRepo = agentSkillRelationRepo;
        this.skillRepo = skillRepo;
    }

    @Override
    public Prompt process(String template, AgentPromptContext context) {
        
        PromptTemplate promptTemplate = SystemPromptTemplate.builder()
            .template(template)
            .renderer(StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build())
            .build();


        long agentId = context.agentId();
        List<PetAiAgentSkillRelationEntity> agentSkillRelations = agentSkillRelationRepo.findByAgentIdAndStatus(agentId, ACTIVE);
        List<Long> skillIds = agentSkillRelations.stream().map(PetAiAgentSkillRelationEntity::getSkillId).toList();
        if (CollectionUtils.isEmpty(skillIds)) {
            return new Prompt(new SystemMessage(template));
        }

        List<PetAiSkillConfigEntity> skills = skillRepo.findBySkillIdInAndStatus(skillIds, ACTIVE);

        String renderedTemplate = promptTemplate.render(Map.of(SKILL_PLACEHOLDER, buildSkillList(skills)));

        return new Prompt(new SystemMessage(renderedTemplate));    
    }

    /**
     * 构建技能列表
     * @param skills
     * @return
     */
    private List<SkillInfoVO> buildSkillList(List<PetAiSkillConfigEntity> skills) {
        return skills.stream().map(SkillInfoVO::new).toList();
    }

    public record SkillInfoVO
    (
        long skillId,
        String skillName,
        String skillDesc
    ) {
        public SkillInfoVO(PetAiSkillConfigEntity skill) {
            this(skill.getSkillId(), skill.getSkillName(), skill.getSkillDesc());
        }
    }
}
