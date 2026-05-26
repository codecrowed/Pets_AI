package jiangxiaopeng.ai.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface InvitationCodeJpaRepository {
    @Select("""
            SELECT id, code, max_uses, used_count, expires_at, status, created_at
            FROM invitation_codes
            WHERE code = #{code}
            LIMIT 1
            """)
    Optional<InvitationCodeJpaEntity> findByCode(String code);

    @Select("""
            SELECT id, code, max_uses, used_count, expires_at, status, created_at
            FROM invitation_codes
            WHERE id = #{id}
            LIMIT 1
            """)
    Optional<InvitationCodeJpaEntity> findById(Long id);

    @Insert("""
            INSERT INTO invitation_codes(code, max_uses, used_count, expires_at, status, created_at)
            VALUES(#{code}, #{maxUses}, #{usedCount}, #{expiresAt}, #{status}, COALESCE(#{createdAt}, now()))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(InvitationCodeJpaEntity entity);

    @Update("""
            UPDATE invitation_codes
            SET code = #{code}, max_uses = #{maxUses}, used_count = #{usedCount}, expires_at = #{expiresAt}, status = #{status}
            WHERE id = #{id}
            """)
    int updateById(InvitationCodeJpaEntity entity);

    default InvitationCodeJpaEntity save(InvitationCodeJpaEntity entity) {
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
