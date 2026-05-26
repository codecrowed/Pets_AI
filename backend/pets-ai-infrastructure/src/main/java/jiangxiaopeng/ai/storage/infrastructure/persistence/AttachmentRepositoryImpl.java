package jiangxiaopeng.ai.storage.infrastructure.persistence;

import jiangxiaopeng.ai.shared.domain.vo.Uid;
import jiangxiaopeng.ai.shared.domain.vo.UserId;
import jiangxiaopeng.ai.storage.domain.model.Attachment;
import jiangxiaopeng.ai.storage.domain.model.FileMetadata;
import jiangxiaopeng.ai.storage.domain.model.StorageKey;
import jiangxiaopeng.ai.storage.domain.repository.AttachmentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AttachmentRepositoryImpl implements AttachmentRepository {

    private final AttachmentJpaRepository jpaRepository;

    public AttachmentRepositoryImpl(AttachmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Attachment save(Attachment attachment) {
        AttachmentJpaEntity entity = toEntity(attachment);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Attachment> findByUid(String uid) {
        return jpaRepository.findByUid(uid).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private AttachmentJpaEntity toEntity(Attachment a) {
        AttachmentJpaEntity e = new AttachmentJpaEntity();
        e.setId(a.getId());
        e.setUid(a.getUid().value());
        e.setUserId(a.getUserId().value());
        e.setMessageId(a.getMessageId());
        e.setOriginalName(a.getMetadata().originalName());
        e.setStorageKey(a.getStorageKey().value());
        e.setContentType(a.getMetadata().contentType());
        e.setFileSize(a.getMetadata().fileSize());
        e.setCreatedAt(a.getCreatedAt());
        return e;
    }

    private Attachment toDomain(AttachmentJpaEntity e) {
        Attachment a = new Attachment();
        a.setId(e.getId());
        a.setUid(new Uid(e.getUid()));
        a.setUserId(new UserId(e.getUserId()));
        a.setMessageId(e.getMessageId());
        a.setMetadata(new FileMetadata(e.getOriginalName(), e.getContentType(), e.getFileSize()));
        a.setStorageKey(new StorageKey(e.getStorageKey()));
        a.setCreatedAt(e.getCreatedAt());
        return a;
    }
}
