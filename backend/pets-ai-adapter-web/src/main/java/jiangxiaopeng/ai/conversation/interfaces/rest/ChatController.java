package jiangxiaopeng.ai.conversation.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jiangxiaopeng.ai.conversation.application.command.CreateChatCommand;
import jiangxiaopeng.ai.conversation.application.dto.ChatListResponse;
import jiangxiaopeng.ai.conversation.application.dto.ChatSummaryDto;
import jiangxiaopeng.ai.conversation.application.service.ChatApplicationService;
import jiangxiaopeng.ai.identity.infrastructure.security.UserPrincipal;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

@Tag(name = "会话", description = "会话创建、列表、搜索、查询、更新与删除")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatApplicationService chatService;

    public ChatController(ChatApplicationService chatService) {
        this.chatService = chatService;
    }

    /** 固定路径须写在 `/{chatId}` 之前，否则 `search` 会被当成 chatId。 */
    @Operation(summary = "创建会话", description = "可选标题与模型，创建新会话并返回摘要。")
    @PostMapping("/createChat")
    public ApiResponse<ChatSummaryDto> createChat(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody(required = false) CreateChatRequest request) {
        var command = new CreateChatCommand(
                user.getUserId(),
                request != null ? request.title() : null,
                request != null ? request.model() : null
        );
        return ApiResponse.ok(chatService.createChat(command));
    }

    @Operation(summary = "会话列表", description = "分页返回当前用户的会话列表。")
    @GetMapping("/listChats")
    public ApiResponse<ChatListResponse> listChats(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(chatService.listChats(user.getUserId(), page, size));
    }

    @Operation(summary = "搜索会话", description = "按关键词搜索会话并分页返回。")
    @GetMapping("/search")
    public ApiResponse<ChatListResponse> searchChats(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(chatService.searchChats(user.getUserId(), q, page, size));
    }

    @Operation(summary = "获取会话详情", description = "根据 chatId 返回会话摘要。")
    @GetMapping("/{chatId}")
    public ApiResponse<ChatSummaryDto> getChat(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String chatId) {
        return ApiResponse.ok(chatService.getChat(chatId, user.getUserId()));
    }

    @Operation(summary = "更新会话", description = "更新标题或默认模型等字段。")
    @PutMapping("/{chatId}")
    public ApiResponse<ChatSummaryDto> updateChat(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String chatId,
            @RequestBody UpdateChatRequest request) {
        return ApiResponse.ok(chatService.updateChat(chatId, user.getUserId(), request.title(), request.model()));
    }

    @Operation(summary = "删除会话", description = "删除指定会话及关联数据（依业务实现）。")
    @DeleteMapping("/{chatId}")
    public ApiResponse<Map<String, Object>> deleteChat(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable String chatId) {
        chatService.deleteChat(chatId, user.getUserId());
        return ApiResponse.okEmpty();
    }

    record CreateChatRequest(String title, String model) {}
    record UpdateChatRequest(String title, String model) {}
}
