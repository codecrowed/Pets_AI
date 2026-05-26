package jiangxiaopeng.ai.shared.infrastructure.web;

import java.util.Collections;
import java.util.Map;

import jiangxiaopeng.ai.shared.exception.ErrorCode;

/**
 * 统一 API 响应体：前端根据 {@code success} 判断成败，业务数据在 {@code data}。
 */
public record ApiResponse<T>(
        String code,
        String message,
        boolean success,
        long timestamp,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("", "", true, System.currentTimeMillis(), data);
    }

    /** 无业务载荷（如 204 语义）时 {@code data} 为 {@code {}} */
    public static ApiResponse<Map<String, Object>> okEmpty() {
        return new ApiResponse<>("", "", true, System.currentTimeMillis(), Collections.emptyMap());
    }

    public static <T> ApiResponse<T> failure(ErrorCode code, String message) {
        String msg = (message != null && !message.isBlank()) ? message : code.getMessage();
        return new ApiResponse<>(code.getCode(), msg, false, System.currentTimeMillis(), null);
    }

    /** 无 {@link ErrorCode} 枚举时的失败响应（如校验、通用异常）。 */
    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(code, message, false, System.currentTimeMillis(), null);
    }
}
