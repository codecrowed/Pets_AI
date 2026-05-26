package jiangxiaopeng.ai.storage.domain.repository;

import jiangxiaopeng.ai.storage.domain.model.Attachment;

import java.util.Optional;

public interface AttachmentRepository {
    Attachment save(Attachment attachment);
    Optional<Attachment> findByUid(String uid);
    void deleteById(Long id);
}
