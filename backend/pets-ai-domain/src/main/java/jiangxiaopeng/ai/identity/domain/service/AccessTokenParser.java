package jiangxiaopeng.ai.identity.domain.service;

import jiangxiaopeng.ai.identity.domain.model.ParsedAccessToken;

import java.util.Optional;

/**
 * 解析并校验访问令牌；实现放在应用层，供基础设施安全过滤器依赖倒置使用。
 */
public interface AccessTokenParser {

    /**
     * @param rawJwt 不含 "Bearer " 前缀的 JWT 字符串
     */
    Optional<ParsedAccessToken> parseValidAccessToken(String rawJwt);
}
