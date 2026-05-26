package jiangxiaopeng.ai.shared.infrastructure.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 将控制器返回的 JSON 业务对象包装为 {@link ApiResponse}（若已是 {@link ApiResponse} 则原样返回）。
 * SSE、二进制下载等不走 Jackson 消息转换器，因此不会被包装。
 */
@RestControllerAdvice(basePackages = "jiangxiaopeng.ai")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverter,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {
        if (body instanceof ApiResponse) {
            return body;
        }
        if (body == null) {
            return ApiResponse.okEmpty();
        }
        return ApiResponse.ok(body);
    }
}
