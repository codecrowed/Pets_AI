package jiangxiaopeng.ai.diet.application.dto;

import java.time.LocalDate;

public record DailyCaloriesDto(
    LocalDate date,
    String dayLabel,
    Integer calories,
    Integer targetCalories
) {}
