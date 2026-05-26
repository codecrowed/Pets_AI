package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UsageRecordJpaRepository {

    @Insert("""
            INSERT INTO usage_records(user_id, session_id, model, tokens_prompt, tokens_completion, recorded_at)
            VALUES(#{userId}, #{sessionId}, #{model}, #{tokensPrompt}, #{tokensCompletion}, COALESCE(#{recordedAt}, now()))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UsageRecordJpaEntity entity);

    default UsageRecordJpaEntity save(UsageRecordJpaEntity entity) {
        insert(entity);
        return entity;
    }
}
