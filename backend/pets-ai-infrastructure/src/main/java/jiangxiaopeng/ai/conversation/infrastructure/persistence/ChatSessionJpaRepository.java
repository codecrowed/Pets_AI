package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatSessionJpaRepository {
    @Select("""
            SELECT id, uid, user_id, title, model, status, created_at, updated_at
            FROM chat_sessions
            WHERE id = #{id}
            LIMIT 1
            """)
    Optional<ChatSessionJpaEntity> findById(Long id);

    @Select("""
            SELECT id, uid, user_id, title, model, status, created_at, updated_at
            FROM chat_sessions
            WHERE uid = #{uid}
            LIMIT 1
            """)
    Optional<ChatSessionJpaEntity> findByUid(String uid);

    @Select("""
            SELECT id, uid, user_id, title, model, status, created_at, updated_at
            FROM chat_sessions
            WHERE user_id = #{userId} AND status = #{status}
            ORDER BY updated_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<ChatSessionJpaEntity> findByUserIdAndStatusPaged(@Param("userId") Long userId, @Param("status") String status, @Param("offset") int offset, @Param("size") int size);

    @Select("""
            SELECT COUNT(1)
            FROM chat_sessions
            WHERE user_id = #{userId} AND status = #{status}
            """)
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("""
            SELECT id, uid, user_id, title, model, status, created_at, updated_at
            FROM chat_sessions
            WHERE user_id = #{userId} AND status = 'ACTIVE' AND LOWER(title) LIKE LOWER(CONCAT('%', #{keyword}, '%'))
            ORDER BY updated_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<ChatSessionJpaEntity> searchByKeywordPaged(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("""
            SELECT COUNT(1)
            FROM chat_sessions
            WHERE user_id = #{userId} AND status = 'ACTIVE' AND LOWER(title) LIKE LOWER(CONCAT('%', #{keyword}, '%'))
            """)
    long countSearchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Insert("""
            INSERT INTO chat_sessions(uid, user_id, title, model, status, created_at, updated_at)
            VALUES(#{uid}, #{userId}, #{title}, #{model}, #{status}, COALESCE(#{createdAt}, now()), COALESCE(#{updatedAt}, now()))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatSessionJpaEntity entity);

    @Update("""
            UPDATE chat_sessions
            SET uid = #{uid}, user_id = #{userId}, title = #{title}, model = #{model}, status = #{status},
                updated_at = COALESCE(#{updatedAt}, now())
            WHERE id = #{id}
            """)
    int updateById(ChatSessionJpaEntity entity);

    @Delete("DELETE FROM chat_sessions WHERE id = #{id}")
    void deleteById(Long id);

    default ChatSessionJpaEntity save(ChatSessionJpaEntity entity) {
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
