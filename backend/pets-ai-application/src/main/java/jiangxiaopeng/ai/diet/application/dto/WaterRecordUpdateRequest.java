package jiangxiaopeng.ai.diet.application.dto;

import jakarta.validation.constraints.Positive;

public record WaterRecordUpdateRequest(
    @Positive(message = "饮水量必须大于0")
    Integer waterAmount,
    
    String recordTime
) {}
