package jiangxiaopeng.ai.shared.context;

/**
 * 请求上下文数据（不可变对象，线程安全）
 * 包含用户信息和当前选择的宠物信息
 */
public record RequestContextData(
        UserContext user,
        PetContext pet
) {
    public static RequestContextData of(UserContext user, PetContext pet) {
        return new RequestContextData(user, pet);
    }

    public static RequestContextData ofUser(UserContext user) {
        return new RequestContextData(user, null);
    }

    public RequestContextData withPet(PetContext pet) {
        return new RequestContextData(this.user, pet);
    }

    public boolean hasUser() {
        return user != null;
    }

    public boolean hasPet() {
        return pet != null;
    }
}
