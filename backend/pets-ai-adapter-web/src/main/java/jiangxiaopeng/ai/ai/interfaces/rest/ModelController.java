package jiangxiaopeng.ai.ai.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jiangxiaopeng.ai.ai.application.dto.ModelInfoDto;
import jiangxiaopeng.ai.ai.application.service.ModelQueryService;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "模型", description = "可选对话模型列表（公开接口，无需 JWT）")
@RestController
@RequestMapping("/api/v1/models")
public class ModelController {

    private final ModelQueryService modelQueryService;

    public ModelController(ModelQueryService modelQueryService) {
        this.modelQueryService = modelQueryService;
    }

    @Operation(summary = "可用模型列表", description = "返回当前环境可用的模型标识与展示名称等。")
    @GetMapping
    public ApiResponse<List<ModelInfoDto>> getAvailableModels() {
        return ApiResponse.ok(modelQueryService.getAvailableModels());
    }
}
