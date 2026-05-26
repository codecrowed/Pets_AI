package jiangxiaopeng.ai.diet.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jiangxiaopeng.ai.diet.application.dto.*;
import jiangxiaopeng.ai.diet.application.service.DietStatsService;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

@Tag(name = "饮食统计", description = "宠物饮食数据统计")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/diet-stats")
public class DietStatsController {

    private final DietStatsService dietStatsService;

    public DietStatsController(DietStatsService dietStatsService) {
        this.dietStatsService = dietStatsService;
    }

    @Operation(summary = "获取当日饮食统计概览")
    @GetMapping("/daily")
    public ApiResponse<DailyDietStatsDto> getDailyStats(
            @RequestParam Long petId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(dietStatsService.getDailyStats(petId, date));
    }

    @Operation(summary = "获取连续记录天数")
    @GetMapping("/continuous-days")
    public ApiResponse<ContinuousDaysDto> getContinuousDays(@RequestParam Long petId) {
        return ApiResponse.ok(dietStatsService.getContinuousDays(petId));
    }

    @Operation(summary = "获取近7天热量趋势")
    @GetMapping("/weekly-calories")
    public ApiResponse<WeeklyCaloriesDto> getWeeklyCalories(
            @RequestParam Long petId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.ok(dietStatsService.getWeeklyCalories(petId, endDate));
    }
}
