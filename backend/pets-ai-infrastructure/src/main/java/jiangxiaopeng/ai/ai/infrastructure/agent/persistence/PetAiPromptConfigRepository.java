package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface PetAiPromptConfigRepository {

    @Select("""
            SELECT id, prompt_id, prompt_type, prompt_value, status, delete_flag
            FROM pet_ai_prompt_config
            WHERE prompt_id = #{promptId} AND status = #{status} AND delete_flag = 0
            LIMIT 1
            """)
    Optional<PetAiPromptConfigEntity> findByPromptIdAndStatus(@Param("promptId") Long promptId, @Param("status") Short status);

    @Select("""
            SELECT id, prompt_id, prompt_type, prompt_value, status, delete_flag
            FROM pet_ai_prompt_config
            WHERE prompt_type = #{promptType} AND status = #{status} AND delete_flag = 0
            ORDER BY id ASC
            LIMIT 1
            """)
    Optional<PetAiPromptConfigEntity> findFirstByPromptTypeAndStatus(@Param("promptType") String promptType, @Param("status") Short status);
}
