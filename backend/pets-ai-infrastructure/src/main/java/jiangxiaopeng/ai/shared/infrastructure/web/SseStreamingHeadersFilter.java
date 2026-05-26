package jiangxiaopeng.ai.shared.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 为流式对话接口补充缓存与代理相关响应头，减少中间层缓冲、便于浏览器/fetch 边收边读。
 */
@Component
public class SseStreamingHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/messages/stream")) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache");
            response.setHeader("X-Accel-Buffering", "no");
        }
        filterChain.doFilter(request, response);
    }
}
