package jiangxiaopeng.ai.diet.application.dto;

import java.util.List;

public record MealGroupDto(
    String mealType,
    String mealTypeLabel,
    String mealIcon,
    Integer totalKcal,
    List<DietRecordDto> items
) {}
