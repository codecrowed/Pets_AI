package jiangxiaopeng.ai.shared.context;

public record UserContext(
    Long uid,
    String email,
    String plan
) {
    public static UserContext of(Long uid, String email, String plan) {
        return new UserContext(uid, email, plan);
    }
} 