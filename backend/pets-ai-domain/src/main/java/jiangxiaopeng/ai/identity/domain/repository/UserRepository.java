package jiangxiaopeng.ai.identity.domain.repository;

import jiangxiaopeng.ai.identity.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUid(String uid);
    Optional<User> findByEmail(String email);
    Optional<User> findByOAuthProviderAndOAuthId(String provider, String oauthId);
    boolean existsByEmail(String email);
}
