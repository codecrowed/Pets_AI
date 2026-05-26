package jiangxiaopeng.ai.diet.infrastructure.persistence;

import jiangxiaopeng.ai.shared.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pet_diet_records")
public class DietRecordEntity extends BaseEntity {

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "meal_type", nullable = false)
    private String mealType;

    @Column(name = "meal_time", nullable = false)
    private Instant mealTime;

    @Column(name = "estimated_kcal")
    private Integer estimatedKcal;

    @Column(name = "protein_g")
    private BigDecimal proteinG;

    @Column(name = "fat_g")
    private BigDecimal fatG;

    @Column(name = "carb_g")
    private BigDecimal carbG;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // Getters and Setters
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
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
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
