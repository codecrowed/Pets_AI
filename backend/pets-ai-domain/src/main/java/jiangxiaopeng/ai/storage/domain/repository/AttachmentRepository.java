package jiangxiaopeng.ai.storage.domain.repository;

import jiangxiaopeng.ai.storage.domain.model.Attachment;

import java.util.Optional;

public interface AttachmentRepository {
    Attachment save(Attachment attachment);
    Optional<Attachment> findByFileIdUid(Long fileId, Long uid);
    void deleteById(Long id);
}
