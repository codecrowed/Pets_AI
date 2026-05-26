package jiangxiaopeng.ai.diet.domain.model;

public enum TargetAge {
    PUPPY("幼年"),
    ADULT("成年"),
    SENIOR("老年"),
    ALL("全年龄");

    private final String label;

    TargetAge(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
