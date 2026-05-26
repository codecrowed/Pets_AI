package jiangxiaopeng.ai.diet.application.dto;

import java.math.BigDecimal;

public record FoodDto(
    Long id,
    String name,
    String icon,
    String category,
    String categoryLabel,
    boolean isStapleFood,
    Long stapleFoodId,
    StapleFoodDto stapleFood,
    Integer kcalPer100g,
    BigDecimal proteinPer100g,
    BigDecimal fatPer100g,
    BigDecimal carbPer100g,
    String description
) {}
