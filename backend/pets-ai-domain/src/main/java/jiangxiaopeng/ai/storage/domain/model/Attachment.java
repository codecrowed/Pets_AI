package jiangxiaopeng.ai.storage.domain.model;

import jiangxiaopeng.ai.shared.domain.vo.Uid;
import jiangxiaopeng.ai.shared.domain.vo.UserId;

import java.time.Instant;

public class Attachment {

    private Long id;
    private Uid uid;
    private UserId userId;
    private Long messageId;
    private FileMetadata metadata;
    private StorageKey storageKey;
    private Instant createdAt;

    public Attachment() {}

    public static Attachment create(UserId userId, FileMetadata metadata, StorageKey storageKey) {
        Attachment attachment = new Attachment();
        attachment.uid = Uid.generate();
        attachment.userId = userId;
        attachment.metadata = metadata;
        attachment.storageKey = storageKey;
        attachment.createdAt = Instant.now();
        return attachment;
    }

    public void associateWithMessage(Long messageId) {
        this.messageId = messageId;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Uid getUid() { return uid; }
    public void setUid(Uid uid) { this.uid = uid; }
    public UserId getUserId() { return userId; }
    public void setUserId(UserId userId) { this.userId = userId; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public FileMetadata getMetadata() { return metadata; }
    public void setMetadata(FileMetadata metadata) { this.metadata = metadata; }
    public StorageKey getStorageKey() { return storageKey; }
    public void setStorageKey(StorageKey storageKey) { this.storageKey = storageKey; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
