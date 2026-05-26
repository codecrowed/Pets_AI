package jiangxiaopeng.ai.identity.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jiangxiaopeng.ai.identity.application.command.LoginCommand;
import jiangxiaopeng.ai.identity.application.command.RefreshTokenCommand;
import jiangxiaopeng.ai.identity.application.command.RegisterCommand;
import jiangxiaopeng.ai.identity.application.dto.AuthResponse;
import jiangxiaopeng.ai.identity.application.service.AuthApplicationService;
import jakarta.validation.Valid;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证", description = "注册、登录、刷新令牌与登出（无需 JWT）")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户注册", description = "创建账号并返回访问令牌与刷新令牌。")
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterCommand command) {
        return ApiResponse.ok(authService.register(command));
    }

    @Operation(summary = "用户登录", description = "使用账号密码登录，返回访问令牌与刷新令牌。")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginCommand command) {
        return ApiResponse.ok(authService.login(command));
    }

    @Operation(summary = "刷新访问令牌", description = "使用 refreshToken 换取新的 accessToken（及可能的新 refreshToken）。")
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenCommand command) {
        return ApiResponse.ok(authService.refreshToken(command));
    }

    @Operation(summary = "登出", description = "可选传入 Authorization Bearer 与请求体中的 refreshToken，服务端使令牌失效。")
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                     @RequestBody(required = false) RefreshTokenCommand body) {
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        String refreshToken = body != null ? body.refreshToken() : null;
        authService.logout(accessToken, refreshToken);
        return ApiResponse.okEmpty();
    }
}
