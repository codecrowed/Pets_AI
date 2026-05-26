package jiangxiaopeng.ai.diet.application.dto;

import jakarta.validation.constraints.NotNull;

public record AiDietAnalysisRequest(
    @NotNull(message = "宠物ID不能为空")
    Long petId,
    
    @NotNull(message = "日期不能为空")
    String date
) {}
