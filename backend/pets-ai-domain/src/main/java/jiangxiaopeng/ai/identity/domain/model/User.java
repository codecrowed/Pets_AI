package jiangxiaopeng.ai.identity.domain.model;

import jiangxiaopeng.ai.shared.domain.vo.Uid;
import jiangxiaopeng.ai.shared.domain.vo.UserId;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;

import java.time.Instant;

public class User {

    private Long id;
    private Uid uid;
    private String username;
    private String email;
    private String avatarUrl;
    private String passwordHash;
    private OAuthIdentity oAuthIdentity;
    private Plan plan;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    public User() {}

    public static User createWithInviteCode(String username, String email, String passwordHash) {
        User user = new User();
        user.uid = Uid.generate();
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.plan = Plan.FREE;
        user.status = UserStatus.ACTIVE;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    public static User createWithOAuth(String username, String email, String avatarUrl, OAuthIdentity oAuthIdentity) {
        User user = new User();
        user.uid = Uid.generate();
        user.username = username;
        user.email = email;
        user.avatarUrl = avatarUrl;
        user.oAuthIdentity = oAuthIdentity;
        user.plan = Plan.FREE;
        user.status = UserStatus.ACTIVE;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    public void updateProfile(String username, String email) {
        if (username != null && !username.isBlank()) {
            this.username = username;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        this.updatedAt = Instant.now();
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        this.updatedAt = Instant.now();
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UserId getUserId() {
        return new UserId(this.id);
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Uid getUid() { return uid; }
    public void setUid(Uid uid) { this.uid = uid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public OAuthIdentity getOAuthIdentity() { return oAuthIdentity; }
    public void setOAuthIdentity(OAuthIdentity oAuthIdentity) { this.oAuthIdentity = oAuthIdentity; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
