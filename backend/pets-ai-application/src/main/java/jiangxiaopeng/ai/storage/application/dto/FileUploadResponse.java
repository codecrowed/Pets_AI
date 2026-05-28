package jiangxiaopeng.ai.storage.application.dto;

public record FileUploadResponse(
        Long id,
        String name,
        String contentType,
        long fileSize,
        String status
) {}
