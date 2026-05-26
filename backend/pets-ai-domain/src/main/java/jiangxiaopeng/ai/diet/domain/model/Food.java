package jiangxiaopeng.ai.diet.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Food {

    private Long id;
    private String name;
    private String icon;
    private FoodCategory category;
    private boolean isStapleFood;
    private Long stapleFoodId;

    // 基础核心指标（每100g）
    private Integer kcalPer100g;
    private BigDecimal proteinPer100g;
    private BigDecimal fatPer100g;
    private BigDecimal carbPer100g;

    // 微量元素（每100g，mg）
    private BigDecimal calciumMg;
    private BigDecimal phosphorusMg;
    private BigDecimal sodiumMg;
    private BigDecimal ironMg;
    private BigDecimal zincMg;

    // 其他营养成分
    private BigDecimal moisturePct;
    private BigDecimal taurineMg;
    private BigDecimal omega3Pct;
    private BigDecimal omega6Pct;

    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    // 关联的主粮对象（非持久化字段）
    private StapleFood stapleFood;

    public Food() {}

    public static Food createStapleFood(String name, String icon, Long stapleFoodId, Integer kcalPer100g,
                                        BigDecimal proteinPer100g, BigDecimal fatPer100g, BigDecimal carbPer100g) {
        Food food = new Food();
        food.name = name;
        food.icon = icon;
        food.category = FoodCategory.STAPLE_FOOD;
        food.isStapleFood = true;
        food.stapleFoodId = stapleFoodId;
        food.kcalPer100g = kcalPer100g;
        food.proteinPer100g = proteinPer100g;
        food.fatPer100g = fatPer100g;
        food.carbPer100g = carbPer100g;
        food.createdAt = Instant.now();
        food.updatedAt = Instant.now();
        return food;
    }

    public static Food createRegularFood(String name, String icon, FoodCategory category, Integer kcalPer100g,
                                         BigDecimal proteinPer100g, BigDecimal fatPer100g, BigDecimal carbPer100g) {
        Food food = new Food();
        food.name = name;
        food.icon = icon;
        food.category = category;
        food.isStapleFood = false;
        food.kcalPer100g = kcalPer100g;
        food.proteinPer100g = proteinPer100g;
        food.fatPer100g = fatPer100g;
        food.carbPer100g = carbPer100g;
        food.createdAt = Instant.now();
        food.updatedAt = Instant.now();
        return food;
    }

    public int calculateKcal(int weightG) {
        if (kcalPer100g == null || kcalPer100g == 0) {
            return 0;
        }
        return Math.round((float) kcalPer100g * weightG / 100);
    }

    public BigDecimal calculateProtein(int weightG) {
        if (proteinPer100g == null) {
            return BigDecimal.ZERO;
        }
        return proteinPer100g.multiply(BigDecimal.valueOf(weightG)).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal calculateFat(int weightG) {
        if (fatPer100g == null) {
            return BigDecimal.ZERO;
        }
        return fatPer100g.multiply(BigDecimal.valueOf(weightG)).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal calculateCarb(int weightG) {
        if (carbPer100g == null) {
            return BigDecimal.ZERO;
        }
        return carbPer100g.multiply(BigDecimal.valueOf(weightG)).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) { this.category = category; }
    public boolean isStapleFood() { return isStapleFood; }
    public void setStapleFood(boolean stapleFood) { isStapleFood = stapleFood; }
    public Long getStapleFoodId() { return stapleFoodId; }
    public void setStapleFoodId(Long stapleFoodId) { this.stapleFoodId = stapleFoodId; }
    public Integer getKcalPer100g() { return kcalPer100g; }
    public void setKcalPer100g(Integer kcalPer100g) { this.kcalPer100g = kcalPer100g; }
    public BigDecimal getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; }
    public BigDecimal getFatPer100g() { return fatPer100g; }
    public void setFatPer100g(BigDecimal fatPer100g) { this.fatPer100g = fatPer100g; }
    public BigDecimal getCarbPer100g() { return carbPer100g; }
    public void setCarbPer100g(BigDecimal carbPer100g) { this.carbPer100g = carbPer100g; }
    public BigDecimal getCalciumMg() { return calciumMg; }
    public void setCalciumMg(BigDecimal calciumMg) { this.calciumMg = calciumMg; }
    public BigDecimal getPhosphorusMg() { return phosphorusMg; }
    public void setPhosphorusMg(BigDecimal phosphorusMg) { this.phosphorusMg = phosphorusMg; }
    public BigDecimal getSodiumMg() { return sodiumMg; }
    public void setSodiumMg(BigDecimal sodiumMg) { this.sodiumMg = sodiumMg; }
    public BigDecimal getIronMg() { return ironMg; }
    public void setIronMg(BigDecimal ironMg) { this.ironMg = ironMg; }
    public BigDecimal getZincMg() { return zincMg; }
    public void setZincMg(BigDecimal zincMg) { this.zincMg = zincMg; }
    public BigDecimal getMoisturePct() { return moisturePct; }
    public void setMoisturePct(BigDecimal moisturePct) { this.moisturePct = moisturePct; }
    public BigDecimal getTaurineMg() { return taurineMg; }
    public void setTaurineMg(BigDecimal taurineMg) { this.taurineMg = taurineMg; }
    public BigDecimal getOmega3Pct() { return omega3Pct; }
    public void setOmega3Pct(BigDecimal omega3Pct) { this.omega3Pct = omega3Pct; }
    public BigDecimal getOmega6Pct() { return omega6Pct; }
    public void setOmega6Pct(BigDecimal omega6Pct) { this.omega6Pct = omega6Pct; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public StapleFood getStapleFoodObj() { return stapleFood; }
    public void setStapleFoodObj(StapleFood stapleFood) { this.stapleFood = stapleFood; }
}
