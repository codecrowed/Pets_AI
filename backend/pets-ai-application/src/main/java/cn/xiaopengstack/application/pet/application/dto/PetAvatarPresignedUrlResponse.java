package jiangxiaopeng.ai.pet.application.dto;

public record PetAvatarPresignedUrlResponse(
        String uploadUrl,
        String avatarKey,
        String publicUrl,
        long expiresInSeconds
) {
}
