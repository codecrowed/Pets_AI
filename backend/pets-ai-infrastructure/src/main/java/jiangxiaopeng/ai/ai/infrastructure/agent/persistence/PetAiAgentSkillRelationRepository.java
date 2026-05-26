package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PetAiAgentSkillRelationRepository {

    @Select("""
            SELECT id, agent_id, skill_id, status, delete_flag
            FROM pet_ai_agent_skill_relation
            WHERE agent_id = #{agentId} AND status = #{status} AND delete_flag = 0
            """)
    List<PetAiAgentSkillRelationEntity> findByAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") Short status);
}
