package jiangxiaopeng.ai.diet.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jiangxiaopeng.ai.diet.application.dto.AiDietAnalysisDto;
import jiangxiaopeng.ai.diet.application.dto.AiDietAnalysisRequest;
import jiangxiaopeng.ai.diet.application.service.AiDietAnalysisService;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

@Tag(name = "AI营养分析", description = "AI驱动的宠物饮食营养分析")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/ai")
public class AiDietAnalysisController {

    private final AiDietAnalysisService aiDietAnalysisService;

    public AiDietAnalysisController(AiDietAnalysisService aiDietAnalysisService) {
        this.aiDietAnalysisService = aiDietAnalysisService;
    }

    @Operation(summary = "生成AI营养分析", description = "根据指定日期的饮食记录生成AI营养分析和建议")
    @PostMapping("/diet-analysis")
    public ApiResponse<AiDietAnalysisDto> analyze(@Valid @RequestBody AiDietAnalysisRequest request) {
        LocalDate date = LocalDate.parse(request.date(), DateTimeFormatter.ISO_DATE);
        return ApiResponse.ok(aiDietAnalysisService.analyze(request.petId(), date));
    }
}
