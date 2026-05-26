package jiangxiaopeng.ai.shared.infrastructure.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pets AI / AI Chat API")
                        .version("1.0")
                        .description("REST API。除「认证」「模型列表」等公开接口外，多数接口需在请求头携带 `Authorization: Bearer <accessToken>`。"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_BEARER_JWT,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("登录或刷新接口返回的 accessToken，在 Swagger 中点击 Authorize 后仅填写 token 即可。")));
    }
}
