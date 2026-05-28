package jiangxiaopeng.ai.storage.domain.model;


import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attachment {

    private Long id;
    private Long uid;
    private Long messageId;
    private FileMetadata metadata;
    private StorageKey storageKey;
    private Instant createdAt;

    public Attachment() {}

    public static Attachment create(Long uid, FileMetadata metadata, StorageKey storageKey) {
        Attachment attachment = new Attachment();
        attachment.uid = uid;
        attachment.metadata = metadata;
        attachment.storageKey = storageKey;
        attachment.createdAt = Instant.now();
        return attachment;
    }

    public void associateWithMessage(Long messageId) {
        this.messageId = messageId;
    }

}
