package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PetAiSkillToolRelationRepository {

    @Select("""
            SELECT id, skill_id, tool_id, status, delete_flag
            FROM pet_ai_skill_tool_relation
            WHERE skill_id = #{skillId} AND status = #{status} AND delete_flag = 0
            """)
    List<PetAiSkillToolRelationEntity> findBySkillIdAndStatus(@Param("skillId") Long skillId, @Param("status") Short status);
}
