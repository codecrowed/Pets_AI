package jiangxiaopeng.ai.diet.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DietRecordAddRequest(
    @NotNull(message = "宠物ID不能为空")
    Long petId,
    
    Long foodId,
    
    @NotBlank(message = "食物名称不能为空")
    String foodName,
    
    @NotNull(message = "重量不能为空")
    @Positive(message = "重量必须大于0")
    Integer weight,
    
    @NotBlank(message = "餐次类型不能为空")
    String mealType,
    
    @NotBlank(message = "用餐时间不能为空")
    String mealTime
) {}
