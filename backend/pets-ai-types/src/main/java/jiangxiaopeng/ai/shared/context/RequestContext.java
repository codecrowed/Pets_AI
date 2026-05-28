package jiangxiaopeng.ai.shared.context;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 全局请求上下文持有者
 * <p>
 * 特性：
 * <ul>
 *   <li>线程安全：使用 InheritableThreadLocal 存储上下文</li>
 *   <li>子线程继承：子线程自动继承父线程的上下文</li>
 *   <li>线程池支持：提供 wrap 方法用于线程池场景</li>
 * </ul>
 * <p>
 * 使用方式：
 * <pre>{@code
 * // 设置上下文（通常在 Filter 中）
 * RequestContext.set(RequestContextData.of(userContext, petContext));
 *
 * // 获取当前用户
 * UserContext user = RequestContext.currentUser().orElseThrow();
 *
 * // 获取当前宠物
 * PetContext pet = RequestContext.currentPet().orElse(null);
 *
 * // 线程池中使用
 * executor.submit(RequestContext.wrap(() -> {
 *     // 这里可以正确获取上下文
 *     UserContext user = RequestContext.requireUser();
 * }));
 *
 * // 清理上下文（通常在 Filter 的 finally 中）
 * RequestContext.clear();
 * }</pre>
 */
public final class RequestContext {

    private static final InheritableThreadLocal<RequestContextData> CONTEXT =
            new InheritableThreadLocal<>();

    private RequestContext() {
    }

    /**
     * 设置当前线程的上下文
     */
    public static void set(RequestContextData data) {
        CONTEXT.set(data);
    }

    /**
     * 获取当前线程的上下文
     */
    public static Optional<RequestContextData> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * 获取当前线程的上下文，如果不存在则抛出异常
     */
    public static RequestContextData require() {
        RequestContextData data = CONTEXT.get();
        if (data == null) {
            throw new IllegalStateException("RequestContext not initialized");
        }
        return data;
    }

    /**
     * 清除当前线程的上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 获取当前用户上下文
     */
    public static Optional<UserContext> currentUser() {
        return get().map(RequestContextData::user);
    }

    /**
     * 获取当前用户上下文，如果不存在则抛出异常
     */
    public static UserContext requireUser() {
        return currentUser().orElseThrow(() ->
                new IllegalStateException("User context not available"));
    }

    /**
     * 获取当前用户ID
     */
    public static Optional<Long> currentUserId() {
        return currentUser().map(UserContext::uid);
    }

    /**
     * 获取当前用户ID，如果不存在则抛出异常
     */
    public static Long requireUserId() {
        return currentUserId().orElseThrow(() ->
                new IllegalStateException("User ID not available"));
    }

    /**
     * 获取当前宠物上下文
     */
    public static Optional<PetContext> currentPet() {
        return get().flatMap(data -> Optional.ofNullable(data.pet()));
    }

    /**
     * 获取当前宠物ID
     */
    public static Optional<Long> currentPetId() {
        return currentPet().map(PetContext::petId);
    }

    /**
     * 设置当前宠物（保留用户信息）
     */
    public static void setPet(PetContext pet) {
        RequestContextData current = CONTEXT.get();
        if (current != null) {
            CONTEXT.set(current.withPet(pet));
        }
    }

    /**
     * 捕获当前上下文快照
     * 用于在线程池中传递上下文
     */
    public static RequestContextData capture() {
        return CONTEXT.get();
    }

    /**
     * 使用指定的上下文执行任务
     * 用于在线程池中恢复上下文
     */
    public static <T> T runWith(RequestContextData context, Supplier<T> task) {
        RequestContextData previous = CONTEXT.get();
        try {
            CONTEXT.set(context);
            return task.get();
        } finally {
            if (previous != null) {
                CONTEXT.set(previous);
            } else {
                CONTEXT.remove();
            }
        }
    }

    /**
     * 使用指定的上下文执行任务（无返回值）
     */
    public static void runWith(RequestContextData context, Runnable task) {
        runWith(context, () -> {
            task.run();
            return null;
        });
    }

    /**
     * 包装 Runnable，使其在执行时携带当前上下文
     * 用于线程池场景
     */
    public static Runnable wrap(Runnable task) {
        RequestContextData captured = capture();
        return () -> runWith(captured, task);
    }

    /**
     * 包装 Callable，使其在执行时携带当前上下文
     * 用于线程池场景
     */
    public static <T> Callable<T> wrap(Callable<T> task) {
        RequestContextData captured = capture();
        return () -> runWith(captured, () -> {
            try {
                return task.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 包装 Supplier，使其在执行时携带当前上下文
     */
    public static <T> Supplier<T> wrap(Supplier<T> task) {
        RequestContextData captured = capture();
        return () -> runWith(captured, task);
    }
}
