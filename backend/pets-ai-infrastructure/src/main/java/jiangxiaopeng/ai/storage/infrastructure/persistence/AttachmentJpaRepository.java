package jiangxiaopeng.ai.storage.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface AttachmentJpaRepository {
    @Select("""
            SELECT id, uid, user_id, message_id, original_name, storage_key, content_type, file_size, created_at
            FROM attachments
            WHERE uid = #{uid}
            LIMIT 1
            """)
    Optional<AttachmentJpaEntity> findByUid(String uid);

    @Select("""
            SELECT id, uid, user_id, message_id, original_name, storage_key, content_type, file_size, created_at
            FROM attachments
            WHERE id = #{id}
            LIMIT 1
            """)
    Optional<AttachmentJpaEntity> findById(Long id);

    @Insert("""
            INSERT INTO attachments(uid, user_id, message_id, original_name, storage_key, content_type, file_size, created_at)
            VALUES(#{uid}, #{userId}, #{messageId}, #{originalName}, #{storageKey}, #{contentType}, #{fileSize}, COALESCE(#{createdAt}, now()))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AttachmentJpaEntity entity);

    @Update("""
            UPDATE attachments
            SET uid = #{uid}, user_id = #{userId}, message_id = #{messageId}, original_name = #{originalName},
                storage_key = #{storageKey}, content_type = #{contentType}, file_size = #{fileSize}
            WHERE id = #{id}
            """)
    int updateById(AttachmentJpaEntity entity);

    @Delete("DELETE FROM attachments WHERE id = #{id}")
    void deleteById(Long id);

    default AttachmentJpaEntity save(AttachmentJpaEntity entity) {
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
