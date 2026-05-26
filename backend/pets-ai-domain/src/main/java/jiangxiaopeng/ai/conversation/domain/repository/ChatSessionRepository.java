package jiangxiaopeng.ai.conversation.domain.repository;

import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ChatSessionRepository {
    ChatSession save(ChatSession session);
    Optional<ChatSession> findByUid(String uid);
    Optional<ChatSession> findById(Long id);
    Page<ChatSession> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
    Page<ChatSession> searchByUserIdAndKeyword(Long userId, String keyword, Pageable pageable);
    void deleteById(Long id);
}
