package jiangxiaopeng.ai.shared.infrastructure.async;

import jiangxiaopeng.ai.shared.context.ContextPropagatingTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置
 * <p>
 * 配置了 ContextPropagatingTaskDecorator，确保异步任务能够正确传递 RequestContext。
 * <p>
 * 使用方式：
 * <pre>{@code
 * @Service
 * public class SomeService {
 *     @Async
 *     public CompletableFuture<String> asyncMethod() {
 *         // 这里可以正确获取 RequestContext
 *         UserContext user = RequestContext.requireUser();
 *         return CompletableFuture.completedFuture("done");
 *     }
 * }
 * }</pre>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        return taskExecutor();
    }
}
