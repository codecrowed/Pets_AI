package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import jiangxiaopeng.ai.conversation.domain.repository.ChatSessionRepository;
import jiangxiaopeng.ai.shared.domain.vo.Uid;
import jiangxiaopeng.ai.shared.domain.vo.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final ChatSessionJpaRepository jpaRepository;

    public ChatSessionRepositoryImpl(ChatSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ChatSession save(ChatSession session) {
        ChatSessionJpaEntity entity = toEntity(session);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<ChatSession> findByUid(String uid) {
        return jpaRepository.findByUid(uid).map(this::toDomain);
    }

    @Override
    public Optional<ChatSession> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<ChatSession> findByUserIdAndStatus(Long userId, String status, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int size = pageable.getPageSize();
        var content = jpaRepository.findByUserIdAndStatusPaged(userId, status, offset, size).stream().map(this::toDomain).toList();
        long total = jpaRepository.countByUserIdAndStatus(userId, status);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ChatSession> searchByUserIdAndKeyword(Long userId, String keyword, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int size = pageable.getPageSize();
        var content = jpaRepository.searchByKeywordPaged(userId, keyword, offset, size).stream().map(this::toDomain).toList();
        long total = jpaRepository.countSearchByKeyword(userId, keyword);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private ChatSessionJpaEntity toEntity(ChatSession session) {
        ChatSessionJpaEntity entity = new ChatSessionJpaEntity();
        entity.setId(session.getId());
        entity.setUid(session.getUid().value());
        entity.setUserId(session.getUserId().value());
        entity.setTitle(session.getTitle());
        entity.setModel(session.getModel());
        entity.setStatus(session.getStatus());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setUpdatedAt(session.getUpdatedAt());
        return entity;
    }

    private ChatSession toDomain(ChatSessionJpaEntity entity) {
        ChatSession session = new ChatSession();
        session.setId(entity.getId());
        session.setUid(new Uid(entity.getUid()));
        session.setUserId(new UserId(entity.getUserId()));
        session.setTitle(entity.getTitle());
        session.setModel(entity.getModel());
        session.setStatus(entity.getStatus());
        session.setCreatedAt(entity.getCreatedAt());
        session.setUpdatedAt(entity.getUpdatedAt());
        return session;
    }
}
