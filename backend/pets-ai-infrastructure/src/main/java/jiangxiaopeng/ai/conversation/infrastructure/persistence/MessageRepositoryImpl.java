package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.model.MessageRole;
import jiangxiaopeng.ai.conversation.domain.model.MessageStatus;
import jiangxiaopeng.ai.conversation.domain.model.TokenUsage;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;
import jiangxiaopeng.ai.shared.domain.vo.Uid;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository jpaRepository;

    public MessageRepositoryImpl(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Message save(Message message) {
        MessageJpaEntity entity = toEntity(message);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Message> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Message> findByUid(String uid) {
        return jpaRepository.findByUid(uid).map(this::toDomain);
    }

    @Override
    public List<Message> findBySessionIdOrderByCreatedAtAsc(Long sessionId) {
        return jpaRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Message> findBySessionIdWithCursor(Long sessionId, Long cursorId, int size) {
        List<MessageJpaEntity> results = jpaRepository.findByCursor(sessionId, cursorId, size);
        List<Message> messages = new ArrayList<>(results.stream().map(this::toDomain).toList());
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public void deleteBySessionId(Long sessionId) {
        jpaRepository.deleteBySessionId(sessionId);
    }

    private MessageJpaEntity toEntity(Message msg) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setId(msg.getId());
        entity.setUid(msg.getUid().value());
        entity.setSessionId(msg.getSessionId());
        entity.setRole(msg.getRole().name());
        entity.setContent(msg.getContent());
        entity.setModel(msg.getModel());
        if (msg.getTokenUsage() != null) {
            entity.setTokensPrompt(msg.getTokenUsage().promptTokens());
            entity.setTokensCompletion(msg.getTokenUsage().completionTokens());
        }
        entity.setStatus(msg.getStatus().name());
        entity.setCreatedAt(msg.getCreatedAt());
        return entity;
    }

    private Message toDomain(MessageJpaEntity entity) {
        Message msg = new Message();
        msg.setId(entity.getId());
        msg.setUid(new Uid(entity.getUid()));
        msg.setSessionId(entity.getSessionId());
        msg.setRole(MessageRole.valueOf(entity.getRole()));
        msg.setContent(entity.getContent());
        msg.setModel(entity.getModel());
        if (entity.getTokensPrompt() != null && entity.getTokensCompletion() != null) {
            msg.setTokenUsage(new TokenUsage(entity.getTokensPrompt(), entity.getTokensCompletion()));
        }
        msg.setStatus(MessageStatus.valueOf(entity.getStatus()));
        msg.setCreatedAt(entity.getCreatedAt());
        return msg;
    }
}
