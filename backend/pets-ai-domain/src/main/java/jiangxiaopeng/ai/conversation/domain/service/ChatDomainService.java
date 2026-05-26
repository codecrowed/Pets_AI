package jiangxiaopeng.ai.conversation.domain.service;

import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatDomainService {

    private final MessageRepository messageRepository;
    private static final int MAX_CONTEXT_MESSAGES = 20;

    public ChatDomainService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<Message> buildContext(Long sessionId) {
        List<Message> allMessages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (allMessages.size() > MAX_CONTEXT_MESSAGES) {
            return allMessages.subList(allMessages.size() - MAX_CONTEXT_MESSAGES, allMessages.size());
        }
        return allMessages;
    }

    public void autoUpdateTitle(ChatSession session, String userMessage) {
        if ("New Chat".equals(session.getTitle()) && userMessage != null) {
            String title = userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage;
            session.updateTitle(title);
        }
    }
}
