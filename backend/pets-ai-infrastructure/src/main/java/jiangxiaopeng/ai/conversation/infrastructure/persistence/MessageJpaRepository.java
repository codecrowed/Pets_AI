package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MessageJpaRepository {
    @Select("""
            SELECT id, uid, session_id, role, content, model, tokens_prompt, tokens_completion, status, created_at
            FROM messages
            WHERE id = #{id}
            LIMIT 1
            """)
    Optional<MessageJpaEntity> findById(Long id);

    @Select("""
            SELECT id, uid, session_id, role, content, model, tokens_prompt, tokens_completion, status, created_at
            FROM messages
            WHERE uid = #{uid}
            LIMIT 1
            """)
    Optional<MessageJpaEntity> findByUidInternal(String uid);

    @Select("""
            SELECT id, uid, session_id, role, content, model, tokens_prompt, tokens_completion, status, created_at
            FROM messages
            WHERE session_id = #{sessionId}
            ORDER BY created_at ASC
            """)
    List<MessageJpaEntity> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Select("""
            SELECT id, uid, session_id, role, content, model, tokens_prompt, tokens_completion, status, created_at
            FROM messages
            WHERE session_id = #{sessionId} AND id < #{cursorId}
            ORDER BY created_at DESC
            LIMIT #{size}
            """)
    List<MessageJpaEntity> findByCursor(@Param("sessionId") Long sessionId, @Param("cursorId") Long cursorId, @Param("size") int size);

    @Insert("""
            INSERT INTO messages(uid, session_id, role, content, model, tokens_prompt, tokens_completion, status, created_at)
            VALUES(#{uid}, #{sessionId}, #{role}, #{content}, #{model}, #{tokensPrompt}, #{tokensCompletion}, #{status}, COALESCE(#{createdAt}, now()))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MessageJpaEntity entity);

    @Update("""
            UPDATE messages
            SET uid = #{uid}, session_id = #{sessionId}, role = #{role}, content = #{content}, model = #{model},
                tokens_prompt = #{tokensPrompt}, tokens_completion = #{tokensCompletion}, status = #{status}
            WHERE id = #{id}
            """)
    int updateById(MessageJpaEntity entity);

    @Delete("DELETE FROM messages WHERE id = #{id}")
    void deleteById(Long id);

    @Delete("DELETE FROM messages WHERE session_id = #{sessionId}")
    void deleteBySessionId(Long sessionId);

    default Optional<MessageJpaEntity> findByUid(String uid) {
        return findByUidInternal(uid);
    }

    default MessageJpaEntity save(MessageJpaEntity entity) {
        if (entity.getId() == null) {
            insert(entity);
        } else if (findById(entity.getId()).isPresent()) {
            updateById(entity);
        } else {
            insert(entity);
        }
        return entity;
    }
}
