package jiangxiaopeng.ai.conversation.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jiangxiaopeng.ai.conversation.application.command.SendMessageCommand;
import jiangxiaopeng.ai.conversation.application.command.SubmitFeedbackCommand;
import jiangxiaopeng.ai.conversation.application.dto.MessageDto;
import jiangxiaopeng.ai.conversation.application.dto.MessageListResponse;
import jiangxiaopeng.ai.conversation.application.service.MessageApplicationService;
import jiangxiaopeng.ai.conversation.application.service.StreamingChatService;
import jiangxiaopeng.ai.identity.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;
import java.util.Map;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

@Tag(name = "消息", description = "会话内消息列表、发送、流式回复、反馈")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/chats/{chatId}/messages")
public class MessageController {

    private final MessageApplicationService messageService;
    private final StreamingChatService streamingChatService;

    public MessageController(MessageApplicationService messageService,
                             StreamingChatService streamingChatService) {
        this.messageService = messageService;
        this.streamingChatService = streamingChatService;
    }

    @Operation(summary = "消息列表", description = "分页拉取消息；cursor 为上一页最后一条消息 ID（可选）。")
    @GetMapping
    public ApiResponse<MessageListResponse> listMessages(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "会话 ID") @PathVariable String chatId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int size) {
        Long cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                cursorId = Long.parseLong(cursor.trim());
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.MSG_004);
            }
        }
        return ApiResponse.ok(messageService.listMessages(chatId, user.getUserId(), cursorId, size));
    }

    @Operation(summary = "发送消息（同步）", description = "发送用户消息并等待完整助手回复。")
    @PostMapping
    public ApiResponse<MessageDto> sendMessage(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "会话 ID") @PathVariable String chatId,
            @Valid @RequestBody SendMessageRequest request) {
        var command = new SendMessageCommand(chatId, user.getUserId(), request.content(), request.attachmentIds(), null);
        return ApiResponse.ok(messageService.sendMessageSync(command));
    }

    private static final long SSE_TIMEOUT_MS = 3 * 60 * 1000L; // 3 minutes

    @Operation(summary = "发送消息（流式 SSE）", description = "返回 text/event-stream，增量推送助手回复。")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter streamMessage(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "会话 ID") @PathVariable String chatId,
            @Valid @RequestBody SendMessageRequest request) {
        var command = new SendMessageCommand(chatId, user.getUserId(), request.content(), request.attachmentIds(), new ResponseBodyEmitter(SSE_TIMEOUT_MS));
        return streamingChatService.execute(command);
    }

    @Operation(summary = "重新生成回复", description = "基于最后一条用户消息流式重新生成助手回复（SSE）。")
    @PostMapping(value = "/{msgId}/regenerate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter regenerateReply(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "会话 ID") @PathVariable String chatId,
            @Parameter(description = "消息 ID") @PathVariable String msgId) {
        // Regenerate reuses the streaming service with the last user message
        var command = new SendMessageCommand(chatId, user.getUserId(), "", null, new ResponseBodyEmitter(SSE_TIMEOUT_MS));
        return streamingChatService.execute(command);
    }

    @Operation(summary = "提交消息反馈", description = "对指定消息提交点赞/点踩等反馈类型。")
    @PostMapping("/{msgId}/feedback")
    public ApiResponse<Map<String, Object>> submitFeedback(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "会话 ID") @PathVariable String chatId,
            @Parameter(description = "消息 ID") @PathVariable String msgId,
            @Valid @RequestBody FeedbackRequest request) {
        messageService.submitFeedback(new SubmitFeedbackCommand(msgId, user.getUserId(), request.type()));
        return ApiResponse.okEmpty();
    }

    @Operation(summary = "撤销消息反馈", description = "移除指定消息上的反馈。")
    @DeleteMapping("/{msgId}/feedback")
    public ApiResponse<Map<String, Object>> removeFeedback(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "会话 ID") @PathVariable String chatId,
            @Parameter(description = "消息 ID") @PathVariable String msgId) {
        messageService.removeFeedback(msgId, user.getUserId());
        return ApiResponse.okEmpty();
    }

    record SendMessageRequest(
            @NotBlank String content,
            List<String> attachmentIds
    ) {
        SendMessageRequest {
            if (attachmentIds == null) {
                attachmentIds = List.of();
            }
        }
    }

    record FeedbackRequest(
            @NotBlank String type
    ) {}
}
