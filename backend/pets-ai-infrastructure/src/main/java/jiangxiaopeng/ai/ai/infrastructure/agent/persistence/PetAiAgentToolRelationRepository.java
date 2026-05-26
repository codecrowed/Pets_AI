package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PetAiAgentToolRelationRepository {

    @Select("""
            SELECT id, agent_id, tool_id, status, delete_flag
            FROM pet_ai_agent_tool_relation
            WHERE agent_id = #{agentId} AND status = #{status} AND delete_flag = 0
            """)
    List<PetAiAgentToolRelationEntity> findByAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") Short status);
}
