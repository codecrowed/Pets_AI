package jiangxiaopeng.ai.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface UserJpaRepository {
    @Select("""
            SELECT id, uid, username, email, avatar_url, password_hash, oauth_provider, oauth_id, plan, status, created_at, updated_at, last_login_at
            FROM users
            WHERE id = #{id}
            LIMIT 1
            """)
    Optional<UserJpaEntity> findById(Long id);

    @Select("""
            SELECT id, uid, username, email, avatar_url, password_hash, oauth_provider, oauth_id, plan, status, created_at, updated_at, last_login_at
            FROM users
            WHERE uid = #{uid}
            LIMIT 1
            """)
    Optional<UserJpaEntity> findByUid(String uid);

    @Select("""
            SELECT id, uid, username, email, avatar_url, password_hash, oauth_provider, oauth_id, plan, status, created_at, updated_at, last_login_at
            FROM users
            WHERE email = #{email}
            LIMIT 1
            """)
    Optional<UserJpaEntity> findByEmail(String email);

    @Select("""
            SELECT id, uid, username, email, avatar_url, password_hash, oauth_provider, oauth_id, plan, status, created_at, updated_at, last_login_at
            FROM users
            WHERE oauth_provider = #{oauthProvider} AND oauth_id = #{oauthId}
            LIMIT 1
            """)
    Optional<UserJpaEntity> findByOauthProviderAndOauthId(@Param("oauthProvider") String oauthProvider, @Param("oauthId") String oauthId);

    @Select("SELECT COUNT(1) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Insert("""
            INSERT INTO users(uid, username, email, avatar_url, password_hash, oauth_provider, oauth_id, plan, status, created_at, updated_at, last_login_at)
            VALUES(#{uid}, #{username}, #{email}, #{avatarUrl}, #{passwordHash}, #{oauthProvider}, #{oauthId}, #{plan}, #{status},
                   COALESCE(#{createdAt}, now()), COALESCE(#{updatedAt}, now()), #{lastLoginAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserJpaEntity entity);

    @Update("""
            UPDATE users
            SET uid = #{uid}, username = #{username}, email = #{email}, avatar_url = #{avatarUrl}, password_hash = #{passwordHash},
                oauth_provider = #{oauthProvider}, oauth_id = #{oauthId}, plan = #{plan}, status = #{status},
                updated_at = COALESCE(#{updatedAt}, now()), last_login_at = #{lastLoginAt}
            WHERE id = #{id}
            """)
    int updateById(UserJpaEntity entity);

    default UserJpaEntity save(UserJpaEntity entity) {
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
