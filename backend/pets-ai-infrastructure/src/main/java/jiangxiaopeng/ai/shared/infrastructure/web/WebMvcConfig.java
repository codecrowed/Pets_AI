package jiangxiaopeng.ai.shared.infrastructure.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthUserContextInterceptor authUserContextInterceptor;
    private final PetContextInterceptor petContextInterceptor;

    public WebMvcConfig(AuthUserContextInterceptor authUserContextInterceptor,
                        PetContextInterceptor petContextInterceptor) {
        this.authUserContextInterceptor = authUserContextInterceptor;
        this.petContextInterceptor = petContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authUserContextInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(petContextInterceptor)
                .addPathPatterns("/api/**");
    }
}
