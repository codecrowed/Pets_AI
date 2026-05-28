package jiangxiaopeng.ai.conversation.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jiangxiaopeng.ai.conversation.application.command.SendMessageCommand;
import jiangxiaopeng.ai.conversation.application.dto.MessageDto;
import jiangxiaopeng.ai.conversation.application.service.MessageApplicationService;
import jiangxiaopeng.ai.conversation.application.service.StreamingChatService;
import jiangxiaopeng.ai.identity.infrastructure.security.UserPrincipal;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponseBodyAdvice;
import jiangxiaopeng.ai.shared.infrastructure.web.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MessageApplicationService messageService;

    @Mock
    private StreamingChatService streamingChatService;

    private static final String CHAT_ID = "test-chat-uid";
    private static final String BASE_URL = "/api/v1/chats/" + CHAT_ID + "/messages";
    private static final Long USER_ID = 1L;
    private static final UserPrincipal TEST_USER =
            new UserPrincipal(USER_ID, "test@example.com", "free");

    @BeforeEach
    void setUp() {
        MessageController controller = new MessageController(messageService, streamingChatService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new ApiResponseBodyAdvice())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                  ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest,
                                                  WebDataBinderFactory binderFactory) {
                        return TEST_USER;
                    }
                })
                .build();
    }

    // =========================================================================
    // sendMessage tests
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/chats/{chatId}/messages - sendMessage")
    class SendMessageTests {

        @Test
        @DisplayName("happy path - returns 200 with MessageDto")
        void happyPath_returns200() throws Exception {
            var expectedDto = new MessageDto(
                    1L, "ASSISTANT", "Hello! How can I help?", null, null,
                    "deepseek", Instant.now()
            );
            when(messageService.sendMessageSync(any(SendMessageCommand.class)))
                    .thenReturn(expectedDto);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content": "Hello AI", "attachmentIds": null}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.role").value("ASSISTANT"))
                    .andExpect(jsonPath("$.data.content").value("Hello! How can I help?"))
                    .andExpect(jsonPath("$.data.model").value("deepseek"));

            verify(messageService).sendMessageSync(any(SendMessageCommand.class));
        }

        @Test
        @DisplayName("happy path with attachmentIds - passes correct command")
        void withAttachments_passesAttachmentsToCommand() throws Exception {
            var expectedDto = new MessageDto(
                    2L, "ASSISTANT", "I see the file.", null, null,
                    "deepseek", Instant.now()
            );
            when(messageService.sendMessageSync(any(SendMessageCommand.class)))
                    .thenReturn(expectedDto);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content": "Check this file", "attachmentIds": ["file-1", "file-2"]}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").value("I see the file."));

            var captor = ArgumentCaptor.forClass(SendMessageCommand.class);
            verify(messageService).sendMessageSync(captor.capture());
            var cmd = captor.getValue();
            assertThat(cmd.chatId()).isEqualTo(CHAT_ID);
            assertThat(cmd.uid()).isEqualTo(USER_ID);
            assertThat(cmd.content()).isEqualTo("Check this file");
            assertThat(cmd.attachmentIds()).containsExactly("file-1", "file-2");
        }

        @Test
        @DisplayName("blank content - returns 400 validation error")
        void blankContent_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content": "", "attachmentIds": null}
                                    """))
                    .andExpect(status().isBadRequest());

            verify(messageService, never()).sendMessageSync(any());
        }

        @Test
        @DisplayName("missing content field - returns 400 validation error")
        void missingContent_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"attachmentIds": null}
                                    """))
                    .andExpect(status().isBadRequest());

            verify(messageService, never()).sendMessageSync(any());
        }

        @Test
        @DisplayName("chat not found - service throws CHAT_001")
        void chatNotFound_returnsErrorStatus() throws Exception {
            when(messageService.sendMessageSync(any(SendMessageCommand.class)))
                    .thenThrow(new BusinessException(ErrorCode.CHAT_001));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content": "Hello", "attachmentIds": null}
                                    """))
                    .andExpect(status().is(ErrorCode.CHAT_001.getHttpStatus().value()));
        }

        @Test
        @DisplayName("ownership violation - service throws CHAT_002")
        void ownershipViolation_returnsErrorStatus() throws Exception {
            when(messageService.sendMessageSync(any(SendMessageCommand.class)))
                    .thenThrow(new BusinessException(ErrorCode.CHAT_002));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"content": "Hello", "attachmentIds": null}
                                    """))
                    .andExpect(status().is(ErrorCode.CHAT_002.getHttpStatus().value()));
        }

        @Test
        @DisplayName("no request body - returns error")
        void noBody_returnsError() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }
    }
}
