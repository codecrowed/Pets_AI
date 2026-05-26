package jiangxiaopeng.ai.conversation.application.service;

import jiangxiaopeng.ai.conversation.application.command.SendMessageCommand;
import jiangxiaopeng.ai.conversation.application.command.SubmitFeedbackCommand;
import jiangxiaopeng.ai.conversation.application.dto.MessageDto;
import jiangxiaopeng.ai.conversation.application.dto.MessageListResponse;
import jiangxiaopeng.ai.conversation.domain.event.AiReplyCompletedEvent;
import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.model.MessageRole;
import jiangxiaopeng.ai.conversation.domain.model.TokenUsage;
import jiangxiaopeng.ai.conversation.domain.repository.ChatSessionRepository;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;
import jiangxiaopeng.ai.conversation.domain.service.AiModelRouter;
import jiangxiaopeng.ai.conversation.domain.service.ChatDomainService;
import jiangxiaopeng.ai.shared.DomainEventPublisher;
import jiangxiaopeng.ai.shared.domain.vo.UserId;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class MessageApplicationService {

    private final ChatSessionRepository chatSessionRepository;
    private final MessageRepository messageRepository;
    private final AiModelRouter aiModelRouter;
    private final ChatDomainService chatDomainService;
    private final DomainEventPublisher eventPublisher;

    public MessageApplicationService(
            ChatSessionRepository chatSessionRepository,
            MessageRepository messageRepository,
            AiModelRouter aiModelRouter,
            ChatDomainService chatDomainService,
            DomainEventPublisher eventPublisher) {
        this.chatSessionRepository = chatSessionRepository;
        this.messageRepository = messageRepository;
        this.aiModelRouter = aiModelRouter;
        this.chatDomainService = chatDomainService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public MessageListResponse listMessages(String chatId, Long userId, Long cursorId, int size) {
        ChatSession session = chatSessionRepository.findByUid(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_001));
        session.validateOwnership(new UserId(userId));

        List<Message> messages;
        boolean hasMore;

        if (cursorId != null) {
            messages = messageRepository.findBySessionIdWithCursor(session.getId(), cursorId, size + 1);
            hasMore = messages.size() > size;
            if (hasMore) {
                messages = new ArrayList<>(messages.subList(1, messages.size()));
            }
        } else {
            List<Message> all = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
            hasMore = all.size() > size;
            if (hasMore) {
                messages = new ArrayList<>(all.subList(all.size() - size, all.size()));
            } else {
                messages = all;
            }
        }

        String nextCursor = hasMore && !messages.isEmpty()
                ? String.valueOf(messages.get(0).getId()) : null;

        List<MessageDto> dtos = messages.stream()
                .map(this::toDto)
                .toList();

        return new MessageListResponse(dtos, nextCursor, hasMore);
    }

    public MessageDto sendMessageSync(SendMessageCommand command) {
        ChatSession session = chatSessionRepository.findByUid(command.chatId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_001));
        session.validateOwnership(new UserId(command.userId()));

        // DomainMessageChatMemoryAdvisor 在 before/after 中自动保存用户消息和 AI 回复
        String conversationId = String.valueOf(session.getId());
        aiModelRouter.complete(conversationId, command.content());

        // advisor 已将 AI 回复持久化（含 model、tokenUsage），从 DB 取最新 ASSISTANT 消息
        List<Message> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        Message aiMsg = findLastAssistant(messages);

        // Auto-update title
        chatDomainService.autoUpdateTitle(session, command.content());
        session.touch();
        chatSessionRepository.save(session);

        // Publish domain event
        eventPublisher.publish(new AiReplyCompletedEvent(
                new UserId(command.userId()), session.getId(), session.getModel(),
                aiMsg.getTokenUsage() != null ? aiMsg.getTokenUsage() : new TokenUsage(0, 0)
        ));

        return toDto(aiMsg);
    }

    private Message findLastAssistant(List<Message> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getRole() == MessageRole.ASSISTANT) {
                return messages.get(i);
            }
        }
        throw new BusinessException(ErrorCode.MSG_003);
    }

    public void submitFeedback(SubmitFeedbackCommand command) {
        Message message = messageRepository.findByUid(command.messageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MSG_003));
        message.submitFeedback(new UserId(command.userId()), command.type());
        messageRepository.save(message);
    }

    public void removeFeedback(String messageId, Long userId) {
        Message message = messageRepository.findByUid(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MSG_003));
        message.clearFeedback();
        messageRepository.save(message);
    }

    private MessageDto toDto(Message msg) {
        return new MessageDto(
                msg.getUid().value(),
                msg.getRole().name(),
                msg.getContent(),
                null,
                msg.getFeedback() != null ? msg.getFeedback().type().name() : null,
                msg.getModel(),
                msg.getCreatedAt()
        );
    }
}
