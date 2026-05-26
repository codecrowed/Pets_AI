package jiangxiaopeng.ai.diet.infrastructure.persistence;

import jiangxiaopeng.ai.shared.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "foods")
public class FoodEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon")
    private String icon;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "is_staple_food", nullable = false)
    private Boolean isStapleFood;

    @Column(name = "staple_food_id")
    private Long stapleFoodId;

    @Column(name = "kcal_per_100g")
    private Integer kcalPer100g;

    @Column(name = "protein_per_100g")
    private BigDecimal proteinPer100g;

    @Column(name = "fat_per_100g")
    private BigDecimal fatPer100g;

    @Column(name = "carb_per_100g")
    private BigDecimal carbPer100g;

    @Column(name = "calcium_mg")
    private BigDecimal calciumMg;

    @Column(name = "phosphorus_mg")
    private BigDecimal phosphorusMg;

    @Column(name = "sodium_mg")
    private BigDecimal sodiumMg;

    @Column(name = "iron_mg")
    private BigDecimal ironMg;

    @Column(name = "zinc_mg")
    private BigDecimal zincMg;

    @Column(name = "moisture_pct")
    private BigDecimal moisturePct;

    @Column(name = "taurine_mg")
    private BigDecimal taurineMg;

    @Column(name = "omega3_pct")
    private BigDecimal omega3Pct;

    @Column(name = "omega6_pct")
    private BigDecimal omega6Pct;

    @Column(name = "description")
    private String description;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getIsStapleFood() { return isStapleFood; }
    public void setIsStapleFood(Boolean isStapleFood) { this.isStapleFood = isStapleFood; }
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
}
