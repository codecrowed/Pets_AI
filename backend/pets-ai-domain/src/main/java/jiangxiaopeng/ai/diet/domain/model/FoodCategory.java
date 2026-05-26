package jiangxiaopeng.ai.diet.domain.model;

public enum FoodCategory {
    STAPLE_FOOD("主粮"),
    MEAT("肉类"),
    VEGETABLE("蔬菜"),
    FRUIT("水果"),
    SNACK("零食"),
    SUPPLEMENT("营养品"),
    OTHER("其他");

    private final String label;

    FoodCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
