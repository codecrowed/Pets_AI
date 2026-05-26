package jiangxiaopeng.ai.identity.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jiangxiaopeng.ai.identity.application.dto.UserInfoDto;
import jiangxiaopeng.ai.identity.application.service.UserApplicationService;
import jiangxiaopeng.ai.identity.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

@Tag(name = "用户", description = "当前登录用户资料与密码")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserApplicationService userService;

    public UserController(UserApplicationService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取当前用户", description = "根据 JWT 返回当前用户资料。")
    @GetMapping("/me")
    public ApiResponse<UserInfoDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.ok(userService.getCurrentUser(user.getUserId()));
    }

    @Operation(summary = "更新当前用户", description = "更新用户名、邮箱等可编辑字段。")
    @PutMapping("/me")
    public ApiResponse<UserInfoDto> updateUser(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(userService.updateUser(user.getUserId(), request.username(), request.email()));
    }

    @Operation(summary = "修改密码", description = "校验旧密码后更新为新密码。")
    @PutMapping("/me/password")
    public ApiResponse<Map<String, Object>> changePassword(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user.getUserId(), request.oldPassword(), request.newPassword());
        return ApiResponse.okEmpty();
    }

    record UpdateUserRequest(
            @Size(max = 64) String username,
            @Email String email
    ) {}

    record ChangePasswordRequest(
            @NotBlank String oldPassword,
            @NotBlank @Size(min = 8) String newPassword
    ) {}
}
