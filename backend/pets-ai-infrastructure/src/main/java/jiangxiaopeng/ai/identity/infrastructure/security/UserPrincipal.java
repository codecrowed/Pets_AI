package jiangxiaopeng.ai.identity.infrastructure.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String uid;
    private final String email;
    private final String plan;

    public UserPrincipal(Long userId, String uid, String email, String plan) {
        this.userId = userId;
        this.uid = uid;
        this.email = email;
        this.plan = plan;
    }

    public Long getUserId() { return userId; }
    public String getUid() { return uid; }
    public String getPlan() { return plan; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return String.valueOf(userId); }

    public String getEmail() { return email; }
}
