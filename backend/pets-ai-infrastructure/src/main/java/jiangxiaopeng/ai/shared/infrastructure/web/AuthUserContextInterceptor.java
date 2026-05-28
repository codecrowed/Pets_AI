package jiangxiaopeng.ai.shared.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jiangxiaopeng.ai.identity.infrastructure.security.UserPrincipal;
import jiangxiaopeng.ai.shared.context.RequestContext;
import jiangxiaopeng.ai.shared.context.RequestContextData;
import jiangxiaopeng.ai.shared.context.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证用户上下文拦截器
 * <p>
 * 从 Spring Security 上下文读取认证后的用户信息，并写入 RequestContext。
 * 在请求完成后清理 RequestContext，避免线程复用导致上下文泄漏。
 */
@Component
public class AuthUserContextInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthUserContextInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return true;
        }

        UserContext userContext = UserContext.of(
            userPrincipal.getUid(),
            userPrincipal.getEmail(),
            userPrincipal.getPlan()
        );
        RequestContext.set(RequestContextData.ofUser(userContext));
        log.debug("Set user context in interceptor: userId={}", userPrincipal.getUid());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContext.clear();
    }
}
