package jiangxiaopeng.ai.ai.infrastructure.agent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentConfigEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentConfigRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentSkillRelationRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentToolRelationEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiAgentToolRelationRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiClientConfigEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiClientConfigRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiPromptConfigEntity;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiPromptConfigRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiSkillConfigRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiSkillToolRelationRepository;
import jiangxiaopeng.ai.ai.infrastructure.agent.persistence.PetAiToolConfigRepository;
import jiangxiaopeng.ai.ai.infrastructure.config.MultiAgentProperties;

/**
 * 从 pet_ai_* 表装配 Agent：client → ChatModel；prompt → 占位符处理；skill 关系 → 技能说明追加；
 * agent_tool + skill_tool → {@link PetAiMethodToolRegistry} 解析 Method Tool。
 */
@Service
public class PetAiAgentRuntimeAssembler {

    private static final short ACTIVE = 1;

    private final MultiAgentProperties multiAgentProperties;
    private final PetAiAgentConfigRepository agentRepo;
    private final PetAiClientConfigRepository clientRepo;
    private final PetAiPromptConfigRepository promptRepo;
    private final PetAiAgentSkillRelationRepository agentSkillRepo;
    private final PetAiSkillConfigRepository skillRepo;
    private final PetAiSkillToolRelationRepository skillToolRepo;
    private final PetAiAgentToolRelationRepository agentToolRepo;
    private final PetAiToolConfigRepository toolRepo;
    private final PetAiClientAssemblyService clientAssembly;
    private final MainAgentSystemPromptProcessor mainAgentSystemPromptProcessor;
    private final DefaultSystemPromptProcessor defaultSystemPromptProcessor;
    private final PetAiToolCallbackResolver toolCallbackResolver;

    public PetAiAgentRuntimeAssembler(
            MultiAgentProperties multiAgentProperties,
            PetAiAgentConfigRepository agentRepo,
            PetAiClientConfigRepository clientRepo,
            PetAiPromptConfigRepository promptRepo,
            PetAiAgentSkillRelationRepository agentSkillRepo,
            PetAiSkillConfigRepository skillRepo,
            PetAiSkillToolRelationRepository skillToolRepo,
            PetAiAgentToolRelationRepository agentToolRepo,
            PetAiToolConfigRepository toolRepo,
            PetAiClientAssemblyService clientAssembly,
            MainAgentSystemPromptProcessor mainAgentSystemPromptProcessor,
            DefaultSystemPromptProcessor defaultSystemPromptProcessor,
            PetAiToolCallbackResolver toolCallbackResolver) {
        this.multiAgentProperties = multiAgentProperties;
        this.agentRepo = agentRepo;
        this.clientRepo = clientRepo;
        this.promptRepo = promptRepo;
        this.agentSkillRepo = agentSkillRepo;
        this.skillRepo = skillRepo;
        this.skillToolRepo = skillToolRepo;
        this.agentToolRepo = agentToolRepo;
        this.toolRepo = toolRepo;
        this.clientAssembly = clientAssembly;
        this.mainAgentSystemPromptProcessor = mainAgentSystemPromptProcessor;
        this.defaultSystemPromptProcessor = defaultSystemPromptProcessor;
        this.toolCallbackResolver = toolCallbackResolver;
    }

    public boolean useSimulation() {
        if (multiAgentProperties.isForceSimulation()) {
            return true;
        }
        return agentRepo.findByAgentIdAndStatus(
                multiAgentProperties.getMainAgentId(), ACTIVE).isEmpty();
    }

    /**
     * 组装主Agent
     * @param ctx
     * @return
     */
    public PetAiAgentRuntime assembleMainAgent(AgentPromptContext ctx) {
        return assemble(
                ctx.withAgentId(multiAgentProperties.getMainAgentId()),
                mainAgentSystemPromptProcessor);
    }

    /**
     * 组装子 Agent：{@code targetAgentId} 为子 Agent 的 {@code pet_ai_agent_config.agent_id}。
     */
    public PetAiAgentRuntime assembleSubAgent(AgentPromptContext ctx) {
        return assemble(ctx, defaultSystemPromptProcessor);
    }

    /**
     * 组装Agent
     * @param ctx
     * @param systemPromptProcessor
     * @return
     */
    private PetAiAgentRuntime assemble(AgentPromptContext ctx, SystemPromptProcessor systemPromptProcessor) {
        Long agentId = ctx.agentId();
        
        // 查找Agent配置
        PetAiAgentConfigEntity agent = agentRepo
                .findByAgentIdAndStatus(agentId, ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Agent not found: " + agentId));
        
        // 查找Client配置
        PetAiClientConfigEntity clientEntity = clientRepo
                .findByClientConfigIdAndStatus(agent.getAgentClientId(), ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Client config not found: " + agent.getAgentClientId()));

        PetAiPromptConfigEntity promptEntity = promptRepo
                .findByPromptIdAndStatus(agent.getAgentPromptId(), ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Agent Prompt config not found: " + agent.getAgentClientId()));
        String modelName = clientEntity.getModel();

        var chatModel = clientAssembly.getOrCreateChatModel(clientEntity, modelName);

        AgentPromptContext fullCtx = ctx.withAgent(agent);

        Prompt base = systemPromptProcessor.process(promptEntity.getPromptValue(), fullCtx);

        List<ToolCallback> tools = resolveToolCallbacks(agentId);
        return new PetAiAgentRuntime(agentId, clientEntity.getClientConfigId(), chatModel, base, tools);
    }



    private LinkedHashSet<Long> collectToolIds(long agentId) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (PetAiAgentToolRelationEntity r : agentToolRepo.findByAgentIdAndStatus(agentId, ACTIVE)) {
            ids.add(r.getToolId());
        }
        return ids;
    }

    private List<ToolCallback> resolveToolCallbacks(long agentId) {
        LinkedHashSet<Long> toolIds = collectToolIds(agentId);
        List<ToolCallback> callbacks = new ArrayList<>();
        for (Long toolId : toolIds) {
            toolRepo.findByToolIdAndStatus(toolId, ACTIVE)
                    .flatMap(toolCallbackResolver::resolve)
                    .ifPresent(callbacks::add);
        }
        return callbacks;
    }
}
