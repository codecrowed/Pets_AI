package jiangxiaopeng.ai.identity.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserInfoDto user
) {}
