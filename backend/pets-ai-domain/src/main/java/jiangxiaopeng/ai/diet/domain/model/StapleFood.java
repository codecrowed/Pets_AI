package jiangxiaopeng.ai.diet.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class StapleFood {

    private Long id;
    private String name;
    private String brand;
    private StapleFoodType foodType;
    private TargetSpecies targetSpecies;
    private String targetBreed;
    private TargetAge targetAge;
    private TargetSize targetSize;

    // 关键营养指标
    private BigDecimal crudeProteinPct;
    private BigDecimal crudeFatPct;
    private BigDecimal crudeAshPct;
    private BigDecimal crudeFiberPct;
    private BigDecimal moisturePct;

    // 其他信息
    private Integer netWeightG;
    private BigDecimal priceYuan;
    private String barcode;
    private String imageUrl;
    private String description;
    private String ingredients;
    private String feedingGuide;

    private Instant createdAt;
    private Instant updatedAt;

    public StapleFood() {}

    public static StapleFood create(String name, String brand, StapleFoodType foodType, TargetSpecies targetSpecies,
                                    BigDecimal crudeProteinPct, BigDecimal crudeFatPct) {
        StapleFood stapleFood = new StapleFood();
        stapleFood.name = name;
        stapleFood.brand = brand;
        stapleFood.foodType = foodType;
        stapleFood.targetSpecies = targetSpecies;
        stapleFood.crudeProteinPct = crudeProteinPct;
        stapleFood.crudeFatPct = crudeFatPct;
        stapleFood.createdAt = Instant.now();
        stapleFood.updatedAt = Instant.now();
        return stapleFood;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public StapleFoodType getFoodType() { return foodType; }
    public void setFoodType(StapleFoodType foodType) { this.foodType = foodType; }
    public TargetSpecies getTargetSpecies() { return targetSpecies; }
    public void setTargetSpecies(TargetSpecies targetSpecies) { this.targetSpecies = targetSpecies; }
    public String getTargetBreed() { return targetBreed; }
    public void setTargetBreed(String targetBreed) { this.targetBreed = targetBreed; }
    public TargetAge getTargetAge() { return targetAge; }
    public void setTargetAge(TargetAge targetAge) { this.targetAge = targetAge; }
    public TargetSize getTargetSize() { return targetSize; }
    public void setTargetSize(TargetSize targetSize) { this.targetSize = targetSize; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
