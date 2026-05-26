package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PetAiToolConfigRepository {

    @Select("""
            SELECT id, tool_id, tool_name, tool_type, return_direct, prompt_id, status, delete_flag
            FROM pet_ai_tool_config
            WHERE tool_id = #{toolId} AND status = #{status} AND delete_flag = 0
            LIMIT 1
            """)
    Optional<PetAiToolConfigEntity> findByToolIdAndStatus(@Param("toolId") Long toolId, @Param("status") Short status);

    @Select({
            "<script>",
            "SELECT id, tool_id, tool_name, tool_type, return_direct, prompt_id, status, delete_flag",
            "FROM pet_ai_tool_config",
            "WHERE tool_id IN",
            "<foreach item='id' collection='toolIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "AND status = #{status} AND delete_flag = 0",
            "</script>"
    })
    List<PetAiToolConfigEntity> findByToolIdInAndStatus(@Param("toolIds") Collection<Long> toolIds, @Param("status") Short status);
}
