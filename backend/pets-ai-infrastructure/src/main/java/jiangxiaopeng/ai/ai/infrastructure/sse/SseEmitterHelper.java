package jiangxiaopeng.ai.ai.infrastructure.sse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Component
@Log4j2
public class SseEmitterHelper {

    private final ObjectMapper objectMapper;

    public SseEmitterHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildSseData(String event, Map<String, Object> data) {
        try {
            return "event: " + event + "\ndata: " + objectMapper.writeValueAsString(data) + "\n\n";
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize SSE data: {}", e.getMessage());
            return "event: " + event + "\ndata: {}\n\n";
        }
    }

    /**
     * 安全地向 emitter 发送数据，处理 emitter 已关闭的情况。
     * @return true 如果发送成功，false 如果 emitter 已关闭或发送失败
     */
    private boolean safeSend(ResponseBodyEmitter emitter, String data, AtomicBoolean emitterClosed) {
        if (emitterClosed.get()) {
            return false;
        }
        try {
            emitter.send(data, MediaType.TEXT_EVENT_STREAM);
            return true;
        } catch (IOException e) {
            log.warn("Failed to send SSE data (IO error): {}", e.getMessage());
            emitterClosed.set(true);
            return false;
        } catch (IllegalStateException e) {
            log.debug("Emitter already completed: {}", e.getMessage());
            emitterClosed.set(true);
            return false;
        }
    }

    public void sendEvent(ResponseBodyEmitter emitter, String event, Map<String, Object> data) {
        try {
            String sseData = buildSseData(event, data);
            emitter.send(sseData, MediaType.TEXT_EVENT_STREAM);
        } catch (IOException e) {
            log.error("Failed to send SSE event: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }

    public void streamToEmitter(
            ResponseBodyEmitter emitter,
            Flux<String> contentFlux,
            String messageId,
            StringBuilder fullContent,
            Runnable onComplete,
            Consumer<Throwable> onError) {

        Flux.concat(
                Flux.just(buildSseData("message_start", Map.of(
                        "messageId", messageId != null ? messageId : "",
                        "role", "ASSISTANT"
                ))),

                contentFlux.map(delta -> {
                    fullContent.append(delta);
                    return buildSseData("content_delta", Map.of("delta", delta));
                }),

                Flux.defer(() -> {
                    if (onComplete != null) {
                        try {
                            onComplete.run();
                        } catch (Exception e) {
                            log.error("Error in onComplete callback: {}", e.getMessage());
                        }
                    }
                    return Flux.just(buildSseData("message_end", Map.of(
                            "messageId", messageId != null ? messageId : "",
                            "finishReason", "stop")));
                }))
                .onErrorResume(e -> {
                    log.error("Stream error: {}", e.getMessage());
                    if (onError != null) {
                        onError.accept(e);
                    }
                    return Flux.just(buildSseData("error", Map.of(
                            "code", "INTERNAL_ERROR",
                            "message", e.getMessage() != null ? e.getMessage() : "Unknown error")));
                })
                .subscribe(
                        eventData -> {
                            try {
                                emitter.send(eventData, MediaType.TEXT_EVENT_STREAM);
                            } catch (IOException e) {
                                log.error("Failed to send SSE data: {}", e.getMessage());
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete);
    }

    public String getFullContent(StringBuilder contentBuilder) {
        return contentBuilder.toString();
    }

    /**
     * 阻塞式流式输出到 emitter，等待流完成后返回完整内容。
     * 用于需要同步返回工具执行结果的场景（如 SubAgentDispatchTool）。
     * 注意：此方法不会关闭 emitter，适用于子 Agent 或中间工具调用。
     */
    public String streamToEmitterBlocking(
            ResponseBodyEmitter emitter,
            Flux<String> contentFlux,
            String messageId,
            Runnable onComplete,
            Consumer<Throwable> onError) {
        return streamToEmitterBlocking(emitter, contentFlux, messageId, onComplete, onError, null, false);
    }

    /**
     * 阻塞式流式输出到 emitter，等待流完成后返回完整内容。
     * 完成后会关闭 emitter，适用于主 Agent 调用。
     */
    public String streamToEmitterBlockingAndComplete(
            ResponseBodyEmitter emitter,
            Flux<String> contentFlux,
            String messageId,
            Runnable onComplete,
            Consumer<Throwable> onError,
            Supplier<String> savedMessageUidSupplier) {
        return streamToEmitterBlocking(emitter, contentFlux, messageId, onComplete, onError, savedMessageUidSupplier, true);
    }

    /**
     * 阻塞式流式输出到 emitter，等待流完成后返回完整内容。
     * 支持在 message_end 事件中返回已保存消息的 UID。
     * 
     * @param savedMessageUidSupplier 获取已保存消息 UID 的回调（在流完成后调用）
     * @param completeEmitter 是否在流完成后关闭 emitter（主 Agent 应设为 true，子 Agent 应设为 false）
     */
    public String streamToEmitterBlocking(
            ResponseBodyEmitter emitter,
            Flux<String> contentFlux,
            String messageId,
            Runnable onComplete,
            Consumer<Throwable> onError,
            Supplier<String> savedMessageUidSupplier,
            boolean completeEmitter) {

        StringBuilder fullContent = new StringBuilder();
        String effectiveMessageId = messageId != null ? messageId : "";
        AtomicBoolean emitterClosed = new AtomicBoolean(false);

        safeSend(emitter, buildSseData("message_start", Map.of(
                "messageId", effectiveMessageId,
                "role", "ASSISTANT"
        )), emitterClosed);

        contentFlux
                .doOnNext(delta -> {
                    log.debug("Streaming delta: {}", delta);
                    fullContent.append(delta);
                    safeSend(emitter, buildSseData("content_delta", Map.of("delta", delta)), emitterClosed);
                })
                .doOnComplete(() -> {
                    if (onComplete != null) {
                        try {
                            onComplete.run();
                        } catch (Exception e) {
                            log.error("Error in onComplete callback: {}", e.getMessage());
                        }
                    }
                    
                    Map<String, Object> endData = new HashMap<>();
                    endData.put("messageId", effectiveMessageId);
                    endData.put("finishReason", "stop");
                    
                    if (savedMessageUidSupplier != null) {
                        try {
                            String savedUid = savedMessageUidSupplier.get();
                            if (savedUid != null) {
                                endData.put("savedMessageUid", savedUid);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to get saved message UID: {}", e.getMessage());
                        }
                    }
                    
                    safeSend(emitter, buildSseData("message_end", endData), emitterClosed);
                    
                    if (completeEmitter && !emitterClosed.get()) {
                        try {
                            emitter.complete();
                        } catch (Exception e) {
                            log.debug("Failed to complete emitter: {}", e.getMessage());
                        }
                    }
                })
                .doOnError(e -> {
                    log.error("Stream error: {}", e.getMessage());
                    if (onError != null) {
                        onError.accept(e);
                    }
                    safeSend(emitter, buildSseData("error", Map.of(
                            "code", "INTERNAL_ERROR",
                            "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
                    )), emitterClosed);
                })
                .onErrorComplete()
                .blockLast();

        return fullContent.toString();
    }
}
