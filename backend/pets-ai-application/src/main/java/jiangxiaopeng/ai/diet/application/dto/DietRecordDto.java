package jiangxiaopeng.ai.diet.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DietRecordDto(
    Long id,
    Long petId,
    Long foodId,
    String foodName,
    String foodIcon,
    Integer weight,
    String mealType,
    String mealTypeLabel,
    Instant mealTime,
    Integer estimatedKcal,
    BigDecimal proteinG,
    BigDecimal fatG,
    BigDecimal carbG,
    Instant createdAt
) {}
