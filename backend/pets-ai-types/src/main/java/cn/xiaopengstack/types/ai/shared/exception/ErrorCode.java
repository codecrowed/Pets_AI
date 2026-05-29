package jiangxiaopeng.ai.shared.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Auth
    AUTH_001("AUTH_001", "邀请码无效或已过期", HttpStatus.BAD_REQUEST),
    AUTH_002("AUTH_002", "Access Token 过期", HttpStatus.UNAUTHORIZED),
    AUTH_003("AUTH_003", "认证失败", HttpStatus.UNAUTHORIZED),
    AUTH_004("AUTH_004", "邮箱已被注册", HttpStatus.CONFLICT),
    AUTH_005("AUTH_005", "Refresh Token 无效", HttpStatus.UNAUTHORIZED),

    // Chat
    CHAT_001("CHAT_001", "会话不存在", HttpStatus.NOT_FOUND),
    CHAT_002("CHAT_002", "无权访问此会话", HttpStatus.FORBIDDEN),

    // Message
    MSG_001("MSG_001", "消息发送失败", HttpStatus.INTERNAL_SERVER_ERROR),
    MSG_002("MSG_002", "AI 服务暂时不可用", HttpStatus.SERVICE_UNAVAILABLE),
    MSG_003("MSG_003", "消息不存在", HttpStatus.NOT_FOUND),
    MSG_004("MSG_004", "分页参数 cursor 无效", HttpStatus.BAD_REQUEST),

    // File
    FILE_001("FILE_001", "文件大小超出限制", HttpStatus.BAD_REQUEST),
    FILE_002("FILE_002", "不支持的文件类型", HttpStatus.BAD_REQUEST),
    FILE_003("FILE_003", "文件不存在", HttpStatus.NOT_FOUND),

    // Rate Limit
    RATE_001("RATE_001", "请求过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS),

    // User
    USER_001("USER_001", "用户不存在", HttpStatus.NOT_FOUND),
    USER_002("USER_002", "原密码错误", HttpStatus.BAD_REQUEST),

    // Pet
    PET_001("PET_001", "宠物不存在", HttpStatus.NOT_FOUND),
    PET_002("PET_002", "无权访问此宠物", HttpStatus.FORBIDDEN),

    // Diet
    DIET_001("DIET_001", "饮食记录不存在", HttpStatus.NOT_FOUND),
    DIET_002("DIET_002", "饮水记录不存在", HttpStatus.NOT_FOUND),

    // Common
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "资源不存在", HttpStatus.NOT_FOUND),
    INVALID_PARAMETER("INVALID_PARAMETER", "参数无效", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
