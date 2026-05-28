package jiangxiaopeng.ai.ai.infrastructure.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Component;

import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentConfigEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentConfigRepository;
import jiangxiaopeng.ai.ai.infrastructure.config.MultiAgentProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 主 Agent 系统提示词处理器
 * 处理主 Agent 系统提示词中的子 Agent 清单占位符
 * 子 Agent 清单占位符：{{subAgentInfoList}}
 * 子 Agent 槽位信息占位符：{{subAgentSlotInfoList}}
 */
@Component
@Slf4j
public class MainAgentSystemPromptProcessor implements SystemPromptProcessor {

    private static final short ACTIVE = 1;
    private static final String AGENT_INFO_LIST = "subAgentInfoList";
    private static final String AGENT_SLOT_INFO_LIST = "subAgentSlotInfoList";

    private final PetAiAgentConfigRepository agentRepo;
    private final MultiAgentProperties multiAgentProperties;

    public MainAgentSystemPromptProcessor(
            PetAiAgentConfigRepository agentRepo,
            MultiAgentProperties multiAgentProperties) {
        this.agentRepo = agentRepo;
        this.multiAgentProperties = multiAgentProperties;
    }

    @Override
    public Prompt process(String template, AgentPromptContext context) {

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(template);
        if (!template.contains(AGENT_INFO_LIST) && !template.contains(AGENT_SLOT_INFO_LIST)) {
            return systemPromptTemplate.create();
        }

        List<PetAiAgentConfigEntity> subAgents =
                    agentRepo.findAllByStatusExcludingAgentId(multiAgentProperties.getMainAgentId(), ACTIVE);
        Prompt prompt = systemPromptTemplate.create(Map.of(AGENT_INFO_LIST, toSubAgentInfoJson(subAgents), AGENT_SLOT_INFO_LIST, toSubAgentSlotJson(subAgents)));
        return prompt;
        
    }

    private List<SubAgentInfo> toSubAgentInfoJson(List<PetAiAgentConfigEntity> subAgents) {
        List<SubAgentInfo> list = new ArrayList<>(subAgents.size());
        for (PetAiAgentConfigEntity e : subAgents) {
            list.add(new SubAgentInfo(e.getAgentId(), e.getAgentName(), e.getAgentDesc()));
        }
        return list;
    }

    private List<String> toSubAgentSlotJson(List<PetAiAgentConfigEntity> subAgents) {
        List<String> list = new ArrayList<>(subAgents.size());
        for (PetAiAgentConfigEntity e : subAgents) {
            list.add(e.getSlot());
        }
        return list;
    }


    public record SubAgentInfo(
        long agentId,
        String agentName,
        String agentDesc
    ){}

    public record SubAgentSlotInfo(
        long agentId,
        String agentName,
        String slot
    ){}
}
