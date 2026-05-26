package jiangxiaopeng.ai.conversation.application.command;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public record SendMessageCommand(
        String chatId,
        Long userId,
        @NotBlank String content,
        List<String> attachmentIds,
        ResponseBodyEmitter emitter
) {}
