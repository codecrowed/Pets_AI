package jiangxiaopeng.ai.identity.infrastructure.persistence;

import jiangxiaopeng.ai.identity.domain.model.*;
import jiangxiaopeng.ai.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUid(String uid) {
        return jpaRepository.findByUid(uid).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByOAuthProviderAndOAuthId(String provider, String oauthId) {
        return jpaRepository.findByOauthProviderAndOauthId(provider, oauthId).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setUid(user.getUid());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setPasswordHash(user.getPasswordHash());
        if (user.getOAuthIdentity() != null) {
            entity.setOauthProvider(user.getOAuthIdentity().provider());
            entity.setOauthId(user.getOAuthIdentity().externalId());
        }
        entity.setPlan(user.getPlan().name());
        entity.setStatus(user.getStatus().name());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setLastLoginAt(user.getLastLoginAt());
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setUid(entity.getUid());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setAvatarUrl(entity.getAvatarUrl());
        user.setPasswordHash(entity.getPasswordHash());
        if (entity.getOauthProvider() != null) {
            user.setOAuthIdentity(new OAuthIdentity(entity.getOauthProvider(), entity.getOauthId()));
        }
        user.setPlan(Plan.valueOf(entity.getPlan()));
        user.setStatus(UserStatus.valueOf(entity.getStatus()));
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());
        user.setLastLoginAt(entity.getLastLoginAt());
        return user;
    }
}
