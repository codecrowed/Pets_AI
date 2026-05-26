package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PetAiSkillConfigRepository {

    @Select("""
            SELECT id, skill_id, skill_name, skill_desc, prompt_id, status, delete_flag
            FROM pet_ai_skill_config
            WHERE skill_id = #{skillId} AND status = #{status} AND delete_flag = 0
            LIMIT 1
            """)
    Optional<PetAiSkillConfigEntity> findBySkillIdAndStatus(Long skillId, Short status);

    @Select({
            "<script>",
            "SELECT id, skill_id, skill_name, skill_desc, prompt_id, status, delete_flag",
            "FROM pet_ai_skill_config",
            "WHERE skill_id IN",
            "<foreach item='id' collection='skillIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "AND status = #{status} AND delete_flag = 0",
            "</script>"
    })
    List<PetAiSkillConfigEntity> findBySkillIdInAndStatus(@Param("skillIds") Collection<Long> skillIds, @Param("status") Short status);
}
