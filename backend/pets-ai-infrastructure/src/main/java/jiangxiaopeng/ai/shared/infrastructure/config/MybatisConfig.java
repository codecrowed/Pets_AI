package jiangxiaopeng.ai.shared.infrastructure.config;

import jiangxiaopeng.ai.shared.infrastructure.persistence.MybatisLogicalDeleteInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
        "jiangxiaopeng.ai.identity.infrastructure.persistence",
        "jiangxiaopeng.ai.conversation.infrastructure.persistence",
        "jiangxiaopeng.ai.storage.infrastructure.persistence",
        "jiangxiaopeng.ai.ai.infrastructure.agent.persistence",
        "jiangxiaopeng.ai.diet.infrastructure.persistence",
        "jiangxiaopeng.ai.pet.infrastructure.persistence"
})
public class MybatisConfig {

    @Bean
    public MybatisLogicalDeleteInterceptor mybatisLogicalDeleteInterceptor() {
        return new MybatisLogicalDeleteInterceptor();
    }
}
