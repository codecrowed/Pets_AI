package jiangxiaopeng.ai.conversation.domain.repository;

import jiangxiaopeng.ai.conversation.domain.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);
    
    Optional<Message> findById(Long id);
    
    Optional<Message> findByMsgIdUid(String msgId, Long uid);
    
    Optional<Message> findByUid(Long uid);
    
    List<Message> findBySessionIdOrderByCreatedAtAsc(Long uid, Long sessionId);
    
    List<Message> findBySessionIdWithCursor(Long uid, Long sessionId, Long cursorId, int size);
    
    void deleteBySessionId(Long sessionId);

    Optional<Message> findLastUserMessage(Long uid, Long sessionId);

    List<Message> findBySessionId(Long sessionId);
}
