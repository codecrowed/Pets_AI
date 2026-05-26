package jiangxiaopeng.ai.storage.application.dto;

public record FileUploadResponse(
        String id,
        String name,
        String contentType,
        long fileSize,
        String status
) {}
