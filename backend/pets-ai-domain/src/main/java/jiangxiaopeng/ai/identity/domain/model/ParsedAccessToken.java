package jiangxiaopeng.ai.identity.domain.model;

/**
 * JWT 访问令牌解析后的身份快照（供接入层构建安全主体，无框架类型）。
 */
public record ParsedAccessToken(
        Long uid,
        String email,
        String plan
) {}
