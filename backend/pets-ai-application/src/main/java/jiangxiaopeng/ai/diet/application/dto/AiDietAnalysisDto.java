package jiangxiaopeng.ai.diet.application.dto;

import java.time.LocalDate;
import java.util.List;

public record AiDietAnalysisDto(
    LocalDate date,
    String summary,
    List<String> suggestions,
    NutritionAnalysisDto nutritionAnalysis
) {
    public record NutritionAnalysisDto(
        String proteinStatus,
        String fatStatus,
        String carbStatus,
        String calorieStatus,
        String overallStatus
    ) {}
}
