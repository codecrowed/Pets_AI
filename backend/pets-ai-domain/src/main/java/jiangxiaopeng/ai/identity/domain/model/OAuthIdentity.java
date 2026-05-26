package jiangxiaopeng.ai.identity.domain.model;

import java.util.Objects;

public record OAuthIdentity(String provider, String externalId) {
    public OAuthIdentity {
        Objects.requireNonNull(provider);
        Objects.requireNonNull(externalId);
    }
}
