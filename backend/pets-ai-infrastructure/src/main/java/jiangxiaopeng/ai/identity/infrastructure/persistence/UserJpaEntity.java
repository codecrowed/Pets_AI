package jiangxiaopeng.ai.identity.infrastructure.persistence;

import jiangxiaopeng.ai.shared.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserJpaEntity extends BaseEntity {

    @Column(name = "uid", nullable = false, unique = true)
    private Long uid;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "oauth_provider")
    private String oauthProvider;

    @Column(name = "oauth_id")
    private String oauthId;

    @Column(name = "plan", nullable = false)
    private String plan;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

}
