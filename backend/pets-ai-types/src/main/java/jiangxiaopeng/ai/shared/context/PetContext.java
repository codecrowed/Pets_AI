package jiangxiaopeng.ai.shared.context;

/**
 * 当前选择的宠物上下文信息（不可变对象，线程安全）
 */
public record PetContext(
        Long petId,
        String petName,
        String species,
        String breed
) {
    public static PetContext of(Long petId, String petName, String species, String breed) {
        return new PetContext(petId, petName, species, breed);
    }
}
