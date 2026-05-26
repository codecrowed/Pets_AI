package jiangxiaopeng.ai.diet.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyDietStatsDto(
    LocalDate date,
    Integer totalKcal,
    BigDecimal proteinG,
    BigDecimal fatG,
    BigDecimal carbG,
    Integer targetKcal,
    Integer waterMl,
    Integer progressPercent,
    List<MealGroupDto> mealGroups
) {}
