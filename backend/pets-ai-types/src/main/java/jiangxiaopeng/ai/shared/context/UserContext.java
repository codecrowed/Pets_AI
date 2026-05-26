package jiangxiaopeng.ai.shared.context;

/**
 * 当前用户上下文信息（不可变对象，线程安全）
 */
public record UserContext(
        Long userId,
        String uid,
        String email,
        String plan
) {
    public static UserContext of(Long userId, String uid, String email, String plan) {
        return new UserContext(userId, uid, email, plan);
    }
}
