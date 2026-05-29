package jiangxiaopeng.ai.conversation.application.service;

import com.alibaba.fastjson.JSONObject;

import jiangxiaopeng.ai.conversation.application.command.GetMessageCommand;
import jiangxiaopeng.ai.conversation.application.command.SendMessageCommand;
import jiangxiaopeng.ai.conversation.application.command.SubmitFeedbackCommand;
import jiangxiaopeng.ai.conversation.application.dto.MessageDto;
import jiangxiaopeng.ai.conversation.application.dto.MessageListResponse;
import jiangxiaopeng.ai.conversation.domain.event.AiReplyCompletedEvent;
import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.model.MessageFeedback;
import jiangxiaopeng.ai.conversation.domain.model.TokenUsage;
import jiangxiaopeng.ai.conversation.domain.repository.ChatSessionRepository;
import jiangxiaopeng.ai.conversation.domain.repository.MessageRepository;
import jiangxiaopeng.ai.conversation.domain.service.AiModelRouter;
import jiangxiaopeng.ai.conversation.domain.service.ChatDomainService;
import jiangxiaopeng.ai.shared.DomainEventPublisher;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageApplicationServiceTest {

    @Mock
    private ChatSessionRepository sessionRepo;
    @Mock
    private MessageRepository messageRepo;
    @Mock
    private AiModelRouter aiModelRouter;
    @Mock
    private ChatDomainService chatDomainService;
    @Mock
    private DomainEventPublisher eventPublisher;

    private MessageApplicationService service;

    private static final Long USER_ID = 1L;
    private static final String CHAT_UID = "test-chat-uid";
    private static final String MODEL = "deepseek";
    private static final String CONTENT = "Hello AI";
    private static final String AI_RESPONSE = "Hi there! How can I help?";

    private ChatSession session;

    @BeforeEach
    void setUp() {
        service = new MessageApplicationService(
                sessionRepo, messageRepo, aiModelRouter,
                chatDomainService, eventPublisher
        );

        session = ChatSession.create(USER_ID, "New Chat", MODEL);
        session.setId(100L);
    }

    // =========================================================================
    // sendMessageSync tests
    // =========================================================================

    @Nested
    @DisplayName("sendMessageSync")
    class SendMessageSyncTests {

        private SendMessageCommand cmd;

        @BeforeEach
        void setUp() {
            cmd = new SendMessageCommand(CHAT_UID, USER_ID, CONTENT, null);
        }

        private void stubCommonMocks() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.of(session));
            when(aiModelRouter.complete(eq("100"), eq(CONTENT))).thenReturn(AI_RESPONSE);

            Message aiMsg = Message.createPendingAiMessage(USER_ID, 100L, "agent-id", MODEL);
            aiMsg.complete(AI_RESPONSE);
            Message userMsg = Message.createUserMessage(USER_ID, 100L, "agent-id", CONTENT);
            when(messageRepo.findBySessionIdOrderByCreatedAtAsc(USER_ID, 100L))
                    .thenReturn(List.of(userMsg, aiMsg));
        }

        @Test
        @DisplayName("happy path returns MessageDto with AI response")
        void happyPath_returnsMessageDto() {
            stubCommonMocks();

            MessageDto result = service.sendMessageSync(cmd);

            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo("ASSISTANT");
            assertThat(result.content()).isEqualTo(AI_RESPONSE);
            assertThat(result.model()).isEqualTo(MODEL);
            assertThat(result.id()).isNotNull();

            System.out.println(JSONObject.toJSONString(result));
        }

        @Test
        @DisplayName("happy path delegates persistence to advisor, queries saved AI message from DB")
        void happyPath_queriesAiMessageFromDb() {
            stubCommonMocks();

            service.sendMessageSync(cmd);

            verify(aiModelRouter).complete("100", CONTENT);
            verify(messageRepo).findBySessionIdOrderByCreatedAtAsc(USER_ID, 100L);
        }

        @Test
        @DisplayName("happy path calls aiModelRouter.complete with conversationId and userMessage")
        void happyPath_callsAiModelRouter() {
            stubCommonMocks();

            service.sendMessageSync(cmd);

            verify(aiModelRouter).complete("100", CONTENT);
        }

        @Test
        @DisplayName("happy path calls autoUpdateTitle")
        void happyPath_callsAutoUpdateTitle() {
            stubCommonMocks();

            service.sendMessageSync(cmd);

            verify(chatDomainService).autoUpdateTitle(session, CONTENT);
        }

        @Test
        @DisplayName("happy path touches and saves session")
        void happyPath_touchesAndSavesSession() {
            stubCommonMocks();

            service.sendMessageSync(cmd);

            verify(sessionRepo).save(session);
            assertThat(session.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("happy path publishes AiReplyCompletedEvent")
        void happyPath_publishesAiReplyCompletedEvent() {
            stubCommonMocks();

            service.sendMessageSync(cmd);

            ArgumentCaptor<AiReplyCompletedEvent> captor = ArgumentCaptor.forClass(AiReplyCompletedEvent.class);
            verify(eventPublisher).publish(captor.capture());

            AiReplyCompletedEvent event = captor.getValue();
            assertThat(event.uid()).isEqualTo(USER_ID);
            assertThat(event.sessionId()).isEqualTo(100L);
            assertThat(event.model()).isEqualTo(MODEL);
        }

        @Test
        @DisplayName("null token usage falls back to TokenUsage(0, 0)")
        void nullTokenUsage_fallsBackToZero() {
            stubCommonMocks();

            service.sendMessageSync(cmd);

            ArgumentCaptor<AiReplyCompletedEvent> captor = ArgumentCaptor.forClass(AiReplyCompletedEvent.class);
            verify(eventPublisher).publish(captor.capture());

            TokenUsage usage = captor.getValue().tokenUsage();
            assertThat(usage.promptTokens()).isEqualTo(0);
            assertThat(usage.completionTokens()).isEqualTo(0);
        }

        @Test
        @DisplayName("session not found throws BusinessException CHAT_001")
        void sessionNotFound_throwsBusinessException() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.sendMessageSync(cmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CHAT_001));
        }

        @Test
        @DisplayName("ownership violation throws BusinessException CHAT_002")
        void ownershipViolation_throwsBusinessException() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.of(session));

            SendMessageCommand wrongUserCmd = new SendMessageCommand(CHAT_UID, 999L, CONTENT, null);

            assertThatThrownBy(() -> service.sendMessageSync(wrongUserCmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CHAT_002));
        }

        @Test
        @DisplayName("AI model router exception propagates")
        void aiError_propagates() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.of(session));
            when(aiModelRouter.complete(eq("100"), eq(CONTENT)))
                    .thenThrow(new RuntimeException("AI service unavailable"));

            assertThatThrownBy(() -> service.sendMessageSync(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("AI service unavailable");

            verify(eventPublisher, never()).publish(any());
        }
    }

    // =========================================================================
    // listMessages tests
    // =========================================================================

    @Nested
    @DisplayName("listMessages")
    class ListMessagesTests {

        @Test
        @DisplayName("returns messages for session without cursor")
        void noCursor_returnsMessages() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.of(session));
            Message msg = Message.createUserMessage(USER_ID, 100L, "agent-id", "hello");
            msg.setId(1L);
            when(messageRepo.findBySessionIdOrderByCreatedAtAsc(USER_ID, 100L)).thenReturn(List.of(msg));

            MessageListResponse response = service.listMessages(new GetMessageCommand(CHAT_UID, USER_ID, null, 50));

            assertThat(response.messages()).hasSize(1);
            assertThat(response.hasMore()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("no cursor: hasMore when total messages exceed page size")
        void noCursor_totalExceedsSize_hasMoreAndNextCursor() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.of(session));
            Message m1 = Message.createUserMessage(USER_ID, 100L, "agent-id", "a");
            m1.setId(1L);
            Message m2 = Message.createUserMessage(USER_ID, 100L, "agent-id", "b");
            m2.setId(2L);
            Message m3 = Message.createUserMessage(USER_ID, 100L, "agent-id", "c");
            m3.setId(3L);
            when(messageRepo.findBySessionIdOrderByCreatedAtAsc(USER_ID, 100L)).thenReturn(List.of(m1, m2, m3));

            MessageListResponse response = service.listMessages(new GetMessageCommand(CHAT_UID, USER_ID, null, 2));

            assertThat(response.messages()).hasSize(2);
            assertThat(response.hasMore()).isTrue();
            assertThat(response.nextCursor()).isEqualTo("2");
        }

        @Test
        @DisplayName("returns messages with cursor-based pagination")
        void withCursor_returnsMessages() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.of(session));
            Message msg = Message.createUserMessage(USER_ID, 100L, "agent-id", "hello");
            msg.setId(5L);
            when(messageRepo.findBySessionIdWithCursor(USER_ID, 100L, 10L, 3))
                    .thenReturn(List.of(msg));

            MessageListResponse response = service.listMessages(new GetMessageCommand(CHAT_UID, USER_ID, 10L, 2));

            assertThat(response.messages()).hasSize(1);
            assertThat(response.hasMore()).isFalse();
        }

        @Test
        @DisplayName("hasMore is true when messages exceed size")
        void exceedsSize_hasMoreTrue() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.of(session));

            Message m1 = Message.createUserMessage(USER_ID, 100L, "agent-id", "msg1");
            m1.setId(1L);
            Message m2 = Message.createUserMessage(USER_ID, 100L, "agent-id", "msg2");
            m2.setId(2L);
            Message m3 = Message.createUserMessage(USER_ID, 100L, "agent-id", "msg3");
            m3.setId(3L);
            when(messageRepo.findBySessionIdWithCursor(USER_ID, 100L, 5L, 3))
                    .thenReturn(List.of(m1, m2, m3));

            MessageListResponse response = service.listMessages(new GetMessageCommand(CHAT_UID, USER_ID, 5L, 2));

            assertThat(response.hasMore()).isTrue();
            assertThat(response.messages()).hasSize(2);
            assertThat(response.nextCursor()).isEqualTo("2");
        }

        @Test
        @DisplayName("session not found throws BusinessException CHAT_001")
        void sessionNotFound_throwsBusinessException() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.listMessages(new GetMessageCommand(CHAT_UID, USER_ID, null, 50)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CHAT_001));
        }

        @Test
        @DisplayName("ownership violation throws BusinessException CHAT_002")
        void ownershipViolation_throwsBusinessException() {
            when(sessionRepo.findByUid(USER_ID)).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> service.listMessages(new GetMessageCommand(CHAT_UID, 999L, null, 50)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CHAT_002));
        }
    }

    // =========================================================================
    // submitFeedback tests
    // =========================================================================

    @Nested
    @DisplayName("submitFeedback")
    class SubmitFeedbackTests {

        @Test
        @DisplayName("happy path submits feedback and saves")
        void happyPath_submitsFeedback() {
            Message msg = Message.createPendingAiMessage(USER_ID, 100L, "agent-id", MODEL);
            msg.complete("response");
            when(messageRepo.findByMsgIdUid("msg-uid", USER_ID)).thenReturn(Optional.of(msg));
            when(messageRepo.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

            service.submitFeedback(new SubmitFeedbackCommand("msg-uid", USER_ID, "LIKE"));

            assertThat(msg.getFeedback()).isNotNull();
            assertThat(msg.getFeedback().type()).isEqualTo(MessageFeedback.FeedbackType.LIKE);
            verify(messageRepo).save(msg);
        }

        @Test
        @DisplayName("message not found throws BusinessException MSG_003")
        void messageNotFound_throwsBusinessException() {
            when(messageRepo.findByMsgIdUid("msg-uid", USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.submitFeedback(
                    new SubmitFeedbackCommand("msg-uid", USER_ID, "LIKE")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.MSG_003));
        }
    }

    // =========================================================================
    // removeFeedback tests
    // =========================================================================

    @Nested
    @DisplayName("removeFeedback")
    class RemoveFeedbackTests {

        @Test
        @DisplayName("happy path clears feedback and saves")
        void happyPath_clearsFeedback() {
            Message msg = Message.createPendingAiMessage(USER_ID, 100L, "agent-id", MODEL);
            msg.complete("response");
            msg.submitFeedback(USER_ID, "LIKE");
            when(messageRepo.findByMsgIdUid("msg-uid", USER_ID)).thenReturn(Optional.of(msg));
            when(messageRepo.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

            service.removeFeedback(new SubmitFeedbackCommand("msg-uid", USER_ID, null));

            assertThat(msg.getFeedback()).isNull();
            verify(messageRepo).save(msg);
        }

        @Test
        @DisplayName("message not found throws BusinessException MSG_003")
        void messageNotFound_throwsBusinessException() {
            when(messageRepo.findByMsgIdUid("msg-uid", USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removeFeedback(new SubmitFeedbackCommand("msg-uid", USER_ID, null)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.MSG_003));
        }
    }
}
