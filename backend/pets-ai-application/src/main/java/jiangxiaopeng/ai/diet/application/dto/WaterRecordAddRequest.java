package jiangxiaopeng.ai.diet.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WaterRecordAddRequest(
    @NotNull(message = "宠物ID不能为空")
    Long petId,
    
    @NotNull(message = "饮水量不能为空")
    @Positive(message = "饮水量必须大于0")
    Integer waterAmount,
    
    @NotNull(message = "记录时间不能为空")
    String recordTime
) {}
