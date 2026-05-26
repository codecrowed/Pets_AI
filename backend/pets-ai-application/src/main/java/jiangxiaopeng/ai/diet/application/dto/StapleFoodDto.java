package jiangxiaopeng.ai.diet.application.dto;

import java.math.BigDecimal;

public record StapleFoodDto(
    Long id,
    String name,
    String brand,
    String foodType,
    String foodTypeLabel,
    String targetSpecies,
    String targetSpeciesLabel,
    String targetBreed,
    String targetAge,
    String targetAgeLabel,
    String targetSize,
    String targetSizeLabel,
    BigDecimal crudeProteinPct,
    BigDecimal crudeFatPct,
    BigDecimal crudeAshPct,
    BigDecimal crudeFiberPct,
    BigDecimal moisturePct,
    String imageUrl,
    String description,
    String ingredients,
    String feedingGuide
) {}
