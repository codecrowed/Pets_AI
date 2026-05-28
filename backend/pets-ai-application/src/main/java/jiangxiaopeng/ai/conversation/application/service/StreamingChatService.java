package jiangxiaopeng.ai.conversation.application.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jiangxiaopeng.ai.ai.domain.service.ChatCompletionService;
import jiangxiaopeng.ai.conversation.application.command.SendMessageCommand;
import jiangxiaopeng.ai.conversation.domain.event.AiReplyCompletedEvent;
import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.model.MessageRole;
import jiangxiaopeng.ai.conversation.domain.model.TokenUsage;
import jiangxiaopeng.ai.conversation.domain.repository.ChatSessionRepository;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;
import jiangxiaopeng.ai.conversation.domain.service.ChatDomainService;
import jiangxiaopeng.ai.shared.DomainEventPublisher;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class StreamingChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatCompletionService chatService;
    private final ChatDomainService chatDomainService;
    private final DomainEventPublisher eventPublisher;
    private final MessageRepository messageRepository;
    private final ExecutorService streamingExecutor = Executors.newCachedThreadPool();


    public StreamingChatService(
            ChatSessionRepository sessionRepo,
            ChatCompletionService chatService,
            ChatDomainService chatDomainService,
            DomainEventPublisher eventPublisher,
            MessageRepository messageRepository) {
        this.sessionRepo = sessionRepo;
        this.chatService = chatService;
        this.chatDomainService = chatDomainService;
        this.eventPublisher = eventPublisher;
        this.messageRepository = messageRepository;
    }

    /**
     * 执行流式聊天：先返回 emitter 给客户端建立 SSE 连接，
     * 然后在异步线程中执行 AI 调用并流式推送数据。
     */
    public ResponseBodyEmitter execute(SendMessageCommand cmd) {
        ResponseBodyEmitter emitter = cmd.emitter();
        
        // 鉴权会话归属
        ChatSession session = sessionRepo.findByChatId(cmd.chatId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_001));
        session.validateOwnership(cmd.uid());

        String conversationId = String.valueOf(session.getId());
        Long sessionId = session.getId();
        String userContent = cmd.content();
        String sessionModel = session.getModel();

        streamingExecutor.submit(() -> {
            try {
                chatService.streamChat(conversationId, userContent, emitter);

                List<Message> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(cmd.uid(), sessionId);
                Message aiMsg = findLastAssistant(messages);

                chatDomainService.autoUpdateTitle(session, userContent);
                session.touch();
                sessionRepo.save(session);

                TokenUsage tokenUsage = (aiMsg != null && aiMsg.getTokenUsage() != null)
                        ? aiMsg.getTokenUsage()
                        : new TokenUsage(0, 0);

                eventPublisher.publish(new AiReplyCompletedEvent(
                        cmd.uid(), sessionId, sessionModel, tokenUsage
                ));
            } catch (Exception e) {
                log.error("Streaming chat error for session {}: {}", sessionId, e.getMessage(), e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 查找最新的 ASSISTANT 消息。
     * 如果找不到，返回 null 而不是抛出异常（处理工具直接返回的情况）。
     */
    private Message findLastAssistant(List<Message> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getRole() == MessageRole.ASSISTANT) {
                return messages.get(i);
            }
        }
        log.warn("No ASSISTANT message found in session, this may happen when tool returns directly");
        return null;
    }

}
