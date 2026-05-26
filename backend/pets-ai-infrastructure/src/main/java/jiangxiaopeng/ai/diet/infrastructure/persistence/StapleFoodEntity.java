package jiangxiaopeng.ai.diet.infrastructure.persistence;

import jiangxiaopeng.ai.shared.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "staple_foods")
public class StapleFoodEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "food_type", nullable = false)
    private String foodType;

    @Column(name = "target_species", nullable = false)
    private String targetSpecies;

    @Column(name = "target_breed")
    private String targetBreed;

    @Column(name = "target_age")
    private String targetAge;

    @Column(name = "target_size")
    private String targetSize;

    @Column(name = "crude_protein_pct", nullable = false)
    private BigDecimal crudeProteinPct;

    @Column(name = "crude_fat_pct", nullable = false)
    private BigDecimal crudeFatPct;

    @Column(name = "crude_ash_pct")
    private BigDecimal crudeAshPct;

    @Column(name = "crude_fiber_pct")
    private BigDecimal crudeFiberPct;

    @Column(name = "moisture_pct")
    private BigDecimal moisturePct;

    @Column(name = "net_weight_g")
    private Integer netWeightG;

    @Column(name = "price_yuan")
    private BigDecimal priceYuan;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "ingredients")
    private String ingredients;

    @Column(name = "feeding_guide")
    private String feedingGuide;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    public String getTargetSpecies() { return targetSpecies; }
    public void setTargetSpecies(String targetSpecies) { this.targetSpecies = targetSpecies; }
    public String getTargetBreed() { return targetBreed; }
    public void setTargetBreed(String targetBreed) { this.targetBreed = targetBreed; }
    public String getTargetAge() { return targetAge; }
    public void setTargetAge(String targetAge) { this.targetAge = targetAge; }
    public String getTargetSize() { return targetSize; }
    public void setTargetSize(String targetSize) { this.targetSize = targetSize; }
    public BigDecimal getCrudeProteinPct() { return crudeProteinPct; }
    public void setCrudeProteinPct(BigDecimal crudeProteinPct) { this.crudeProteinPct = crudeProteinPct; }
    public BigDecimal getCrudeFatPct() { return crudeFatPct; }
    public void setCrudeFatPct(BigDecimal crudeFatPct) { this.crudeFatPct = crudeFatPct; }
    public BigDecimal getCrudeAshPct() { return crudeAshPct; }
    public void setCrudeAshPct(BigDecimal crudeAshPct) { this.crudeAshPct = crudeAshPct; }
    public BigDecimal getCrudeFiberPct() { return crudeFiberPct; }
    public void setCrudeFiberPct(BigDecimal crudeFiberPct) { this.crudeFiberPct = crudeFiberPct; }
    public BigDecimal getMoisturePct() { return moisturePct; }
    public void setMoisturePct(BigDecimal moisturePct) { this.moisturePct = moisturePct; }
    public Integer getNetWeightG() { return netWeightG; }
    public void setNetWeightG(Integer netWeightG) { this.netWeightG = netWeightG; }
    public BigDecimal getPriceYuan() { return priceYuan; }
    public void setPriceYuan(BigDecimal priceYuan) { this.priceYuan = priceYuan; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getFeedingGuide() { return feedingGuide; }
    public void setFeedingGuide(String feedingGuide) { this.feedingGuide = feedingGuide; }
}
