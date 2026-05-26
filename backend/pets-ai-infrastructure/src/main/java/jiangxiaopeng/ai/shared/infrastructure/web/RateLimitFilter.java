package jiangxiaopeng.ai.shared.infrastructure.web;

import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final int requestsPerMinute;
    private final int streamingRequestsPerMinute;

    public RateLimitFilter(
            RedisTemplate<String, String> redisTemplate,
            @Value("${app.rate-limit.requests-per-minute:30}") int requestsPerMinute,
            @Value("${app.rate-limit.streaming-requests-per-minute:10}") int streamingRequestsPerMinute) {
        this.redisTemplate = redisTemplate;
        this.requestsPerMinute = requestsPerMinute;
        this.streamingRequestsPerMinute = streamingRequestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // Skip rate limiting for auth and actuator endpoints
        if (path.startsWith("/api/v1/auth/") || path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // String userId = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : request.getRemoteAddr();
        // boolean isStreaming = path.contains("/stream");
        // int limit = isStreaming ? streamingRequestsPerMinute : requestsPerMinute;
        // String key = "rate_limit:" + (isStreaming ? "stream:" : "api:") + userId;

        // try {
        //     Long count = redisTemplate.opsForValue().increment(key);
        //     if (count != null && count == 1) {
        //         redisTemplate.expire(key, Duration.ofMinutes(1));
        //     }
        //     if (count != null && count > limit) {
        //         throw new BusinessException(ErrorCode.RATE_001);
        //     }
        // } catch (BusinessException e) {
        //     throw e;
        // } catch (Exception e) {
        //     // If Redis is down, allow the request through
        //     logger.warn("Rate limiting unavailable: " + e.getMessage());
        // }

        filterChain.doFilter(request, response);
    }
}
