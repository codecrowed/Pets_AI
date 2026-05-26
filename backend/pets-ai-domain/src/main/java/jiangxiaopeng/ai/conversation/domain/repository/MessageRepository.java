package jiangxiaopeng.ai.conversation.domain.repository;

import jiangxiaopeng.ai.conversation.domain.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);
    Optional<Message> findById(Long id);
    Optional<Message> findByUid(String uid);
    List<Message> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    List<Message> findBySessionIdWithCursor(Long sessionId, Long cursorId, int size);
    void deleteBySessionId(Long sessionId);
}
