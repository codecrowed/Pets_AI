package jiangxiaopeng.ai.diet.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jiangxiaopeng.ai.diet.application.dto.*;
import jiangxiaopeng.ai.diet.application.service.WaterRecordService;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

@Tag(name = "饮水记录", description = "宠物饮水记录管理")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/water-records")
public class WaterRecordController {

    private final WaterRecordService waterRecordService;

    public WaterRecordController(WaterRecordService waterRecordService) {
        this.waterRecordService = waterRecordService;
    }

    @Operation(summary = "获取当日饮水统计")
    @GetMapping("/daily")
    public ApiResponse<DailyWaterStatsDto> getDailyWaterStats(
            @RequestParam Long petId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(waterRecordService.getDailyWaterStats(petId, date));
    }

    @Operation(summary = "添加饮水记录")
    @PostMapping
    public ApiResponse<WaterRecordDto> add(@Valid @RequestBody WaterRecordAddRequest request) {
        return ApiResponse.ok(waterRecordService.addRecord(request));
    }

    @Operation(summary = "编辑饮水记录")
    @PutMapping("/{recordId}")
    public ApiResponse<WaterRecordDto> update(
            @PathVariable Long recordId,
            @Valid @RequestBody WaterRecordUpdateRequest request) {
        return ApiResponse.ok(waterRecordService.updateRecord(recordId, request));
    }

    @Operation(summary = "删除饮水记录")
    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> delete(@PathVariable Long recordId) {
        waterRecordService.deleteRecord(recordId);
        return ApiResponse.ok(null);
    }
}
