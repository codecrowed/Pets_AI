package jiangxiaopeng.ai.diet.application.dto;

import java.util.List;

public record WeeklyCaloriesDto(
    List<DailyCaloriesDto> dailyCalories,
    Integer avgCalories,
    Integer targetCalories
) {}
