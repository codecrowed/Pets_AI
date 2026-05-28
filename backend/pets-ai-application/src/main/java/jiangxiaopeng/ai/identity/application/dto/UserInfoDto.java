package jiangxiaopeng.ai.identity.application.dto;

public record UserInfoDto(
        Long uid,
        String username,
        String email,
        String avatarUrl,
        String plan
) {}
