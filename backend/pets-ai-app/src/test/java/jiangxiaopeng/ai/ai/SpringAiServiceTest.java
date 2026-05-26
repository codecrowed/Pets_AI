package jiangxiaopeng.ai.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import jiangxiaopeng.ai.ai.infrastructure.springai.SpringAiChatService;
import reactor.core.publisher.Flux;

@SpringBootTest
public class SpringAiServiceTest {

    @Resource
    private SpringAiChatService springAiChatService;

    @Test
    @DisplayName("测试SpringAiService")
    public void test() {
        String conversationId = "1234567890";

        // String chat = springAiChatService.chat(conversationId, "我想在家里训练我家狗狗定点上厕所该怎么做？ 狗狗年龄1岁，品种是金毛");
        // System.out.println(chat);

        Flux<ChatResponse> streamChat = springAiChatService.streamChat(conversationId, "我想在家里训练我家狗狗定点上厕所该怎么做？ 狗狗年龄1岁，品种是金毛");
        System.out.println(streamChat);
    }
}
