package jiangxiaopeng.ai.identity.application.dto;

public record UserInfoDto(
        String uid,
        String username,
        String email,
        String avatarUrl,
        String plan
) {}
