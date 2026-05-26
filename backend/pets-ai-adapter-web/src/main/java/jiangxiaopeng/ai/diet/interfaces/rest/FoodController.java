package jiangxiaopeng.ai.diet.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jiangxiaopeng.ai.diet.application.dto.FoodDto;
import jiangxiaopeng.ai.diet.application.service.FoodService;
import jiangxiaopeng.ai.identity.infrastructure.security.UserPrincipal;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

@Tag(name = "食物库", description = "食物搜索与常用食物")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @Operation(summary = "搜索食物库", description = "同时搜索普通食物和主粮，主粮优先展示")
    @GetMapping("/search")
    public ApiResponse<List<FoodDto>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(foodService.searchFoods(keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取常用食物列表", description = "返回当前用户最常使用的食物")
    @GetMapping("/frequent")
    public ApiResponse<List<FoodDto>> getFrequent(@AuthenticationPrincipal UserPrincipal user) {
        return ApiResponse.ok(foodService.getFrequentFoods(user.getUserId()));
    }
}
