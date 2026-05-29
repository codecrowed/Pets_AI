package jiangxiaopeng.ai.pet.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建宠物命令
 */
public record PetCreateCommand(
        @NotBlank(message = "宠物昵称不能为空")
        @Size(max = 64, message = "宠物昵称不能超过64个字符")
        String name,

        @NotNull(message = "宠物类型不能为空")
        String species,

        @Size(max = 128, message = "品种不能超过128个字符")
        String breed,

        LocalDate birthday,

        BigDecimal weightKg,

        String gender,

        Boolean neutered,

        Boolean microchipped,

        String avatarUrl,

        @Size(max = 16, message = "emoji不能超过16个字符")
        String avatarEmoji,

        @Size(max = 512, message = "过敏史不能超过512个字符")
        String allergies,

        @Size(max = 512, message = "慢性疾病不能超过512个字符")
        String chronicConditions,

        @Size(max = 256, message = "主食品牌不能超过256个字符")
        String mainFoodBrand,

        @Size(max = 256, message = "常去医院不能超过256个字符")
        String vetHospital,

        String notes
) {}
