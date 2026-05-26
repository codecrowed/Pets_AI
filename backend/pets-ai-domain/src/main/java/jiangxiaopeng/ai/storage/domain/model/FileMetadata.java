package jiangxiaopeng.ai.storage.domain.model;

import java.util.Objects;

public record FileMetadata(String originalName, String contentType, long fileSize) {
    public FileMetadata {
        Objects.requireNonNull(originalName);
        Objects.requireNonNull(contentType);
        if (fileSize <= 0) throw new IllegalArgumentException("File size must be positive");
    }
}
