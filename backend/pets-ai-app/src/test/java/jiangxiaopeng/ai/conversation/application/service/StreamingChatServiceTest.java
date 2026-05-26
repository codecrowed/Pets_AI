package jiangxiaopeng.ai.conversation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jiangxiaopeng.ai.conversation.application.command.SendMessageCommand;
import jiangxiaopeng.ai.conversation.domain.event.AiReplyCompletedEvent;
import jiangxiaopeng.ai.conversation.domain.model.ChatSession;
import jiangxiaopeng.ai.conversation.domain.model.Message;
import jiangxiaopeng.ai.conversation.domain.model.TokenUsage;
import jiangxiaopeng.ai.conversation.domain.repository.ChatSessionRepository;
import jiangxiaopeng.ai.conversation.domain.service.AiModelRouter;
import jiangxiaopeng.ai.conversation.domain.service.ChatDomainService;
import jiangxiaopeng.ai.conversation.domain.service.ChatMemoryStore;
import jiangxiaopeng.ai.shared.DomainEventPublisher;
import jiangxiaopeng.ai.shared.domain.vo.UserId;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class StreamingChatServiceTest {

    @Mock
    private ChatSessionRepository sessionRepo;
    @Mock
    private ChatMemoryStore chatMemoryStore;
    @Mock
    private AiModelRouter aiModelRouter;
    @Mock
    private ChatDomainService chatDomainService;
    @Mock
    private DomainEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private StreamingChatService service;

    private static final Long USER_ID = 1L;
    private static final String CHAT_UID = "test-chat-uid";
    private static final String MODEL = "deepseek";
    private static final String CONTENT = "Hello AI";

    private ChatSession session;
    private SendMessageCommand cmd;

    @BeforeEach
    void setUp() {
        service = new StreamingChatService(
                sessionRepo, chatMemoryStore, aiModelRouter,
                chatDomainService, eventPublisher, objectMapper
        );

        session = ChatSession.create(new UserId(USER_ID), "New Chat", MODEL);
        session.setId(100L);

        cmd = new SendMessageCommand(CHAT_UID, USER_ID, CONTENT, null);
    }

    private Message createPendingAiMsg() {
        return Message.createPendingAiMessage(100L, MODEL);
    }

    private void stubCommonMocks() {
        when(sessionRepo.findByUid(CHAT_UID)).thenReturn(Optional.of(session));

        Message aiMsg = createPendingAiMsg();
        when(chatMemoryStore.savePendingAiMessage(100L, MODEL)).thenReturn(aiMsg);
        when(chatMemoryStore.completeAiMessage(any(Message.class), any(String.class)))
                .thenAnswer(inv -> {
                    Message msg = inv.getArgument(0);
                    msg.complete(inv.getArgument(1));
                    return msg;
                });

        when(aiModelRouter.streamComplete(eq("100"), eq(CONTENT))).thenReturn(Flux.just("Hello"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseData(ServerSentEvent<String> sse) {
        try {
            return objectMapper.readValue(sse.data(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================================
    // Happy path tests
    // =========================================================================

    @Test
    @DisplayName("execute: happy path emits message_start, content_delta(s), and message_end")
    void execute_happyPath_emitsStartDeltasAndEnd() {
        stubCommonMocks();

        Flux<ServerSentEvent<String>> result = service.execute(cmd);

        StepVerifier.create(result)
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("message_start");
                    Map<String, Object> data = parseData(sse);
                    assertThat(data.get("role")).isEqualTo("ASSISTANT");
                    assertThat(data.get("model")).isEqualTo(MODEL);
                    assertThat(data.get("messageId")).isNotNull();
                })
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("content_delta");
                    Map<String, Object> data = parseData(sse);
                    assertThat(data.get("delta")).isEqualTo("Hello");
                })
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("message_end");
                    Map<String, Object> data = parseData(sse);
                    assertThat(data.get("finishReason")).isEqualTo("stop");
                    assertThat(data.get("messageId")).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("execute: happy path saves user message then AI message via ChatMemoryStore")
    void execute_happyPath_savesUserAndAiMessages() {
        stubCommonMocks();

        service.execute(cmd).blockLast();

        verify(chatMemoryStore).saveUserMessage(100L, CONTENT);
        verify(chatMemoryStore).savePendingAiMessage(100L, MODEL);
        verify(chatMemoryStore).completeAiMessage(any(Message.class), eq("Hello"));
    }

    @Test
    @DisplayName("execute: happy path publishes AiReplyCompletedEvent with correct fields")
    void execute_happyPath_publishesAiReplyCompletedEvent() {
        stubCommonMocks();

        service.execute(cmd).blockLast();

        ArgumentCaptor<AiReplyCompletedEvent> captor = ArgumentCaptor.forClass(AiReplyCompletedEvent.class);
        verify(eventPublisher).publish(captor.capture());

        AiReplyCompletedEvent event = captor.getValue();
        assertThat(event.userId()).isEqualTo(new UserId(USER_ID));
        assertThat(event.sessionId()).isEqualTo(100L);
        assertThat(event.model()).isEqualTo(MODEL);
    }

    @Test
    @DisplayName("execute: happy path accumulates full content from multiple tokens")
    void execute_happyPath_accumulatesFullContent() {
        when(sessionRepo.findByUid(CHAT_UID)).thenReturn(Optional.of(session));

        Message aiMsg = createPendingAiMsg();
        when(chatMemoryStore.savePendingAiMessage(100L, MODEL)).thenReturn(aiMsg);
        when(chatMemoryStore.completeAiMessage(any(Message.class), any(String.class)))
                .thenAnswer(inv -> {
                    Message msg = inv.getArgument(0);
                    msg.complete(inv.getArgument(1));
                    return msg;
                });

        when(aiModelRouter.streamComplete(eq("100"), eq(CONTENT))).thenReturn(Flux.just("Hello", " ", "World"));

        service.execute(cmd).blockLast();

        verify(chatMemoryStore).completeAiMessage(any(Message.class), eq("Hello World"));
    }

    @Test
    @DisplayName("execute: null token usage falls back to TokenUsage(0, 0)")
    void execute_nullTokenUsage_fallsBackToZero() {
        stubCommonMocks();

        service.execute(cmd).blockLast();

        ArgumentCaptor<AiReplyCompletedEvent> captor = ArgumentCaptor.forClass(AiReplyCompletedEvent.class);
        verify(eventPublisher).publish(captor.capture());

        TokenUsage usage = captor.getValue().tokenUsage();
        assertThat(usage.promptTokens()).isEqualTo(0);
        assertThat(usage.completionTokens()).isEqualTo(0);
    }

    @Test
    @DisplayName("execute: happy path touches and saves session")
    void execute_happyPath_touchesAndSavesSession() {
        stubCommonMocks();

        service.execute(cmd).blockLast();

        verify(sessionRepo).save(session);
        assertThat(session.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("execute: happy path calls autoUpdateTitle")
    void execute_happyPath_callsAutoUpdateTitle() {
        stubCommonMocks();

        service.execute(cmd).blockLast();

        verify(chatDomainService).autoUpdateTitle(session, CONTENT);
    }

    // =========================================================================
    // Error scenario tests
    // =========================================================================

    @Test
    @DisplayName("execute: session not found throws BusinessException CHAT_001")
    void execute_sessionNotFound_throwsBusinessException() {
        when(sessionRepo.findByUid(CHAT_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(cmd))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getErrorCode()).isEqualTo(ErrorCode.CHAT_001);
                });
    }

    @Test
    @DisplayName("execute: ownership violation throws BusinessException CHAT_002")
    void execute_ownershipViolation_throwsBusinessException() {
        when(sessionRepo.findByUid(CHAT_UID)).thenReturn(Optional.of(session));

        SendMessageCommand wrongUserCmd = new SendMessageCommand(CHAT_UID, 999L, CONTENT, null);

        assertThatThrownBy(() -> service.execute(wrongUserCmd))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getErrorCode()).isEqualTo(ErrorCode.CHAT_002);
                });
    }

    @Test
    @DisplayName("execute: AI stream error emits error SSE with INTERNAL_ERROR code")
    void execute_aiStreamError_emitsErrorSse() {
        when(sessionRepo.findByUid(CHAT_UID)).thenReturn(Optional.of(session));

        Message aiMsg = createPendingAiMsg();
        when(chatMemoryStore.savePendingAiMessage(100L, MODEL)).thenReturn(aiMsg);

        when(aiModelRouter.streamComplete(eq("100"), eq(CONTENT)))
                .thenReturn(Flux.error(new RuntimeException("AI service unavailable")));

        Flux<ServerSentEvent<String>> result = service.execute(cmd);

        StepVerifier.create(result)
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("message_start");
                })
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("error");
                    Map<String, Object> data = parseData(sse);
                    assertThat(data.get("code")).isEqualTo("INTERNAL_ERROR");
                    assertThat(data.get("message")).isEqualTo("AI service unavailable");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("execute: AI stream error with null message emits 'Unknown error'")
    void execute_aiStreamErrorWithNullMessage_emitsUnknownError() {
        when(sessionRepo.findByUid(CHAT_UID)).thenReturn(Optional.of(session));

        Message aiMsg = createPendingAiMsg();
        when(chatMemoryStore.savePendingAiMessage(100L, MODEL)).thenReturn(aiMsg);

        when(aiModelRouter.streamComplete(eq("100"), eq(CONTENT)))
                .thenReturn(Flux.error(new RuntimeException((String) null)));

        Flux<ServerSentEvent<String>> result = service.execute(cmd);

        StepVerifier.create(result)
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("message_start");
                })
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("error");
                    Map<String, Object> data = parseData(sse);
                    assertThat(data.get("code")).isEqualTo("INTERNAL_ERROR");
                    assertThat(data.get("message")).isEqualTo("Unknown error");
                })
                .verifyComplete();
    }
}
