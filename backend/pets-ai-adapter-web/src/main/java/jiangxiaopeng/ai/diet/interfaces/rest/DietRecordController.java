package jiangxiaopeng.ai.diet.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jiangxiaopeng.ai.diet.application.dto.*;
import jiangxiaopeng.ai.diet.application.service.DietRecordService;
import jiangxiaopeng.ai.identity.infrastructure.security.UserPrincipal;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

@Tag(name = "饮食记录", description = "宠物饮食记录管理")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/diet-records")
public class DietRecordController {

    private final DietRecordService dietRecordService;

    public DietRecordController(DietRecordService dietRecordService) {
        this.dietRecordService = dietRecordService;
    }

    @Operation(summary = "获取指定日期饮食记录列表")
    @GetMapping("/by-date")
    public ApiResponse<List<DietRecordDto>> getByDate(
            @RequestParam Long petId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(dietRecordService.getRecordsByDate(petId, date));
    }

    @Operation(summary = "添加单条饮食记录")
    @PostMapping
    public ApiResponse<DietRecordDto> add(
            @Valid @RequestBody DietRecordAddRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.ok(dietRecordService.addRecord(request, user.getUid()));
    }

    @Operation(summary = "编辑单条饮食记录")
    @PutMapping("/{recordId}")
    public ApiResponse<DietRecordDto> update(
            @PathVariable Long recordId,
            @Valid @RequestBody DietRecordUpdateRequest request) {
        return ApiResponse.ok(dietRecordService.updateRecord(recordId, request));
    }

    @Operation(summary = "删除单条饮食记录")
    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> delete(@PathVariable Long recordId) {
        dietRecordService.deleteRecord(recordId);
        return ApiResponse.ok(null);
    }
}
