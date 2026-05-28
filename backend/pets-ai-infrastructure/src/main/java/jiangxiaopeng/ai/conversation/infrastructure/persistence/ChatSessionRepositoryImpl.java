package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import jiangxiaopeng.ai.conversation.domain.repository.ChatSessionRepository;

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
    public Optional<ChatSession> findByUid(Long uid) {
        return jpaRepository.findByUid(uid).map(this::toDomain);
    }

    @Override
    public Optional<ChatSession> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<ChatSession> findByUserIdAndStatus(Long uid, String status, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int size = pageable.getPageSize();
        var content = jpaRepository.findByUserIdAndStatusPaged(uid, status, offset, size).stream().map(this::toDomain).toList();
        long total = jpaRepository.countByUserIdAndStatus(uid, status);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ChatSession> searchByUserIdAndKeyword(Long uid, String keyword, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int size = pageable.getPageSize();
        var content = jpaRepository.searchByKeywordPaged(uid, keyword, offset, size).stream().map(this::toDomain).toList();
        long total = jpaRepository.countSearchByKeyword(uid, keyword);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private ChatSessionJpaEntity toEntity(ChatSession session) {
        ChatSessionJpaEntity entity = new ChatSessionJpaEntity();
        entity.setChatId(session.getChatId());
        entity.setUid(session.getUid());
        entity.setTitle(session.getTitle());
        entity.setModel(session.getModel());
        entity.setStatus(session.getStatus());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setUpdatedAt(session.getUpdatedAt());
        return entity;
    }

    private ChatSession toDomain(ChatSessionJpaEntity entity) {
        ChatSession session = new ChatSession();
        session.setChatId(entity.getChatId());
        session.setUid(entity.getUid());
        session.setTitle(entity.getTitle());
        session.setModel(entity.getModel());
        session.setStatus(entity.getStatus());
        session.setCreatedAt(entity.getCreatedAt());
        session.setUpdatedAt(entity.getUpdatedAt());
        return session;
    }

    @Override
    public Optional<ChatSession> findByChatId(String chatId) {
        return jpaRepository.findByChatId(chatId).map(this::toDomain);
    }
}
