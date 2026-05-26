package jiangxiaopeng.ai.shared.context;

import org.springframework.core.task.TaskDecorator;

/**
 * Spring 异步任务装饰器，用于在线程池中传播请求上下文
 * <p>
 * 配置方式：
 * <pre>{@code
 * @Configuration
 * @EnableAsync
 * public class AsyncConfig implements AsyncConfigurer {
 *     @Override
 *     public Executor getAsyncExecutor() {
 *         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 *         executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
 *         executor.initialize();
 *         return executor;
 *     }
 * }
 * }</pre>
 */
public class ContextPropagatingTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        return RequestContext.wrap(runnable);
    }
}
