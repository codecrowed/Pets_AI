package jiangxiaopeng.ai.identity.infrastructure.security;

import jiangxiaopeng.ai.identity.domain.model.ParsedAccessToken;
import jiangxiaopeng.ai.identity.domain.service.AccessTokenParser;
import jiangxiaopeng.ai.shared.context.RequestContext;
import jiangxiaopeng.ai.shared.context.RequestContextData;
import jiangxiaopeng.ai.shared.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenParser accessTokenParser;

    public JwtAuthenticationFilter(AccessTokenParser accessTokenParser) {
        this.accessTokenParser = accessTokenParser;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);
            Optional<ParsedAccessToken> parsed = token != null
                    ? accessTokenParser.parseValidAccessToken(token)
                    : Optional.empty();
            if (parsed.isPresent()) {
                ParsedAccessToken p = parsed.get();
                UserPrincipal principal = new UserPrincipal(
                        p.uid(), p.email(), p.plan());
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                UserContext userContext = UserContext.of(p.uid(), p.email(), p.plan());
                RequestContext.set(RequestContextData.ofUser(userContext));
            }

            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
