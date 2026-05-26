package jiangxiaopeng.ai.diet.domain.model;

public enum TargetSpecies {
    DOG("狗"),
    CAT("猫"),
    ALL("通用");

    private final String label;

    TargetSpecies(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
