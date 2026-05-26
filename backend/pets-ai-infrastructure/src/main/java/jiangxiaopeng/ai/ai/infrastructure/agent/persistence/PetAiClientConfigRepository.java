package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface PetAiClientConfigRepository {

    @Select("""
            SELECT id, client_config_id, provider, api_key, base_url, model, completions_path, embeddings_path, status, delete_flag
            FROM pet_ai_client_config
            WHERE client_config_id = #{clientConfigId} AND status = #{status} AND delete_flag = 0
            LIMIT 1
            """)
    Optional<PetAiClientConfigEntity> findByClientConfigIdAndStatus(@Param("clientConfigId") Long clientConfigId, @Param("status") Short status);
}
