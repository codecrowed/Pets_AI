package jiangxiaopeng.ai.conversation.infrastructure.ai;

import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;
import jiangxiaopeng.ai.conversation.domain.service.ChatMemoryStore;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapts the domain MessageRepository to both Spring AI's ChatMemoryRepository
 * and our domain ChatMemoryStore. Centralizes all message read/write for chat memory.
 */
@Component
public class JpaChatMemoryRepository implements ChatMemoryRepository, ChatMemoryStore {

    private final MessageRepository messageRepository;

    public JpaChatMemoryRepository(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // =========================================================================
    // ChatMemoryStore — domain message persistence
    // =========================================================================

    @Override
    public Message saveUserMessage(Long uid, Long sessionId, String agentId, String content) {
        Message msg = Message.createUserMessage(uid, sessionId, agentId, content);
        return messageRepository.save(msg);
    }

    @Override
    public Message savePendingAiMessage(Long uid, Long sessionId, String agentId, String model) {
        Message msg = Message.createPendingAiMessage(uid, sessionId, agentId, model);
        return messageRepository.save(msg);
    }

    @Override
    public Message completeAiMessage(Message aiMsg, String fullContent) {
        aiMsg.complete(fullContent);
        return messageRepository.save(aiMsg);
    }

    // =========================================================================
    // ChatMemoryRepository — Spring AI adapter (read-only for advisor)
    // =========================================================================

    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<org.springframework.ai.chat.messages.Message> findByConversationId(String conversationId) {
        if ("default".equals(conversationId)) {
            return List.of();
        }
        Long sessionId = Long.parseLong(conversationId);
        List<Message> messages = messageRepository.findBySessionId(sessionId);
        return messages.stream()
                .map(this::toSpringAiMessage)
                .toList();
    }

    @Override
    public void saveAll(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
        // No-op: 消息持久化由 DomainMessageChatMemoryAdvisor 直接通过 MessageRepository 处理，
        // 无需再经过 ChatMemoryRepository 转换。
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Long sessionId = Long.parseLong(conversationId);
        messageRepository.deleteBySessionId(sessionId);
    }

    private org.springframework.ai.chat.messages.Message toSpringAiMessage(Message msg) {
        return switch (msg.getRole()) {
            case USER -> new UserMessage(msg.getContent());
            case ASSISTANT -> new AssistantMessage(msg.getContent());
            case SYSTEM -> new SystemMessage(msg.getContent());
        };
    }
}
