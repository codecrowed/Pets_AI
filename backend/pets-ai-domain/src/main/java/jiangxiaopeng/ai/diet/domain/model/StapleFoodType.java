package jiangxiaopeng.ai.diet.domain.model;

public enum StapleFoodType {
    COMPLETE_DOG("全价狗粮"),
    COMPLETE_CAT("全价猫粮"),
    PRESCRIPTION("处方粮"),
    PUPPY("幼犬粮"),
    KITTEN("幼猫粮"),
    SENIOR("老年粮"),
    INDOOR("室内粮"),
    WEIGHT_CONTROL("体重控制粮");

    private final String label;

    StapleFoodType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
