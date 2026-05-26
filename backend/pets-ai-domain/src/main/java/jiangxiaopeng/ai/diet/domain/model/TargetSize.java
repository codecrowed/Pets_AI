package jiangxiaopeng.ai.diet.domain.model;

public enum TargetSize {
    SMALL("小型"),
    MEDIUM("中型"),
    LARGE("大型"),
    ALL("通用");

    private final String label;

    TargetSize(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
