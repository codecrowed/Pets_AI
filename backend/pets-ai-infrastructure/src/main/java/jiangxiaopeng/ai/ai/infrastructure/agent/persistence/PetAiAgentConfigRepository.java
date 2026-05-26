package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PetAiAgentConfigRepository {

    @Select("""
            SELECT id, agent_id, agent_client_id, agent_name, agent_desc, agent_prompt_id, slot, status, delete_flag
            FROM pet_ai_agent_config
            WHERE agent_id = #{agentId} AND status = #{status} AND delete_flag = 0
            LIMIT 1
            """)
    Optional<PetAiAgentConfigEntity> findByAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") Short status);

    /**
     * 除主 Agent 外的全部子 Agent（用于主 Agent 系统提示中的子 Agent 清单占位符）。
     */
    @Select("""
            SELECT id, agent_id, agent_client_id, agent_name, agent_desc, agent_prompt_id, slot, status, delete_flag
            FROM pet_ai_agent_config
            WHERE agent_id <> #{mainAgentId} AND status = #{status} AND delete_flag = 0
            ORDER BY agent_id
            """)
    List<PetAiAgentConfigEntity> findAllByStatusExcludingAgentId(
            @Param("mainAgentId") Long mainAgentId,
            @Param("status") Short status);
}
