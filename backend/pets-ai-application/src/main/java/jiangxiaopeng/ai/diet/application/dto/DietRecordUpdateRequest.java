package jiangxiaopeng.ai.diet.application.dto;

import jakarta.validation.constraints.Positive;

public record DietRecordUpdateRequest(
    String foodName,
    
    @Positive(message = "重量必须大于0")
    Integer weight,
    
    String mealType,
    
    String mealTime
) {}
