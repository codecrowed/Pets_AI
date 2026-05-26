package jiangxiaopeng.ai.diet.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DietRecord {

    private Long id;
    private Long petId;
    private Long foodId;
    private String foodName;
    private Integer weight;
    private MealType mealType;
    private Instant mealTime;
    private Integer estimatedKcal;
    private BigDecimal proteinG;
    private BigDecimal fatG;
    private BigDecimal carbG;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    // 关联的食物对象（非持久化字段）
    private Food food;

    public DietRecord() {}

    public static DietRecord create(Long petId, Long foodId, String foodName, Integer weight,
                                    MealType mealType, Instant mealTime) {
        DietRecord record = new DietRecord();
        record.petId = petId;
        record.foodId = foodId;
        record.foodName = foodName;
        record.weight = weight;
        record.mealType = mealType;
        record.mealTime = mealTime;
        record.createdAt = Instant.now();
        record.updatedAt = Instant.now();
        return record;
    }

    public void calculateNutrition(Food food) {
        if (food != null && weight != null && weight > 0) {
            this.estimatedKcal = food.calculateKcal(weight);
            this.proteinG = food.calculateProtein(weight);
            this.fatG = food.calculateFat(weight);
            this.carbG = food.calculateCarb(weight);
        }
    }

    public void update(String foodName, Integer weight, MealType mealType, Instant mealTime) {
        if (foodName != null) this.foodName = foodName;
        if (weight != null) this.weight = weight;
        if (mealType != null) this.mealType = mealType;
        if (mealTime != null) this.mealTime = mealTime;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public LocalDate getMealDate() {
        return mealTime.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public Instant getMealTime() { return mealTime; }
    public void setMealTime(Instant mealTime) { this.mealTime = mealTime; }
    public Integer getEstimatedKcal() { return estimatedKcal; }
    public void setEstimatedKcal(Integer estimatedKcal) { this.estimatedKcal = estimatedKcal; }
    public BigDecimal getProteinG() { return proteinG; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }
    public BigDecimal getFatG() { return fatG; }
    public void setFatG(BigDecimal fatG) { this.fatG = fatG; }
    public BigDecimal getCarbG() { return carbG; }
    public void setCarbG(BigDecimal carbG) { this.carbG = carbG; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }
}
