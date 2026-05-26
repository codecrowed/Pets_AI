package jiangxiaopeng.ai.identity.application.service;

import jiangxiaopeng.ai.identity.domain.model.ParsedAccessToken;
import jiangxiaopeng.ai.identity.domain.model.User;
import jiangxiaopeng.ai.identity.domain.service.AccessTokenParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtTokenService implements AccessTokenParser {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration,
            RedisTemplate<String, String> redisTemplate) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.redisTemplate = redisTemplate;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claims(Map.of(
                        "uid", user.getUid().value(),
                        "email", user.getEmail() != null ? user.getEmail() : "",
                        "plan", user.getPlan().name()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claims(Map.of("type", "refresh"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date()) && !isTokenBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = parseToken(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set("blacklist:" + token, "1", Duration.ofMillis(ttl));
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isTokenBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    @Override
    public Optional<ParsedAccessToken> parseValidAccessToken(String rawJwt) {
        if (!isTokenValid(rawJwt)) {
            return Optional.empty();
        }
        try {
            Claims claims = parseToken(rawJwt);
            long userId = Long.parseLong(claims.getSubject());
            String uid = claims.get("uid", String.class);
            String email = claims.get("email", String.class);
            String plan = claims.get("plan", String.class);
            return Optional.of(new ParsedAccessToken(
                    userId,
                    uid != null ? uid : "",
                    email != null ? email : "",
                    plan != null ? plan : ""));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
