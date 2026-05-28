package jiangxiaopeng.ai.conversation.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetMessageCommand {
    /**
     * 会话 ID
     */
    private String chatId;
    /**
     * 用户 ID
     */
    private Long uid;
    /**
     * 游标 ID
     */
    private Long cursorId;
    /**
     * 每页大小
     */
    private int size;
}
