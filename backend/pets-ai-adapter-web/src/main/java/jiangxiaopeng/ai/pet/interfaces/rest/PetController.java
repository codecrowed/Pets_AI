package jiangxiaopeng.ai.pet.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jiangxiaopeng.ai.identity.infrastructure.security.UserPrincipal;
import jiangxiaopeng.ai.pet.application.PetApplicationService;
import jiangxiaopeng.ai.pet.application.dto.PetAvatarPresignedUrlResponse;
import jiangxiaopeng.ai.pet.application.dto.PetAvatarUploadResponse;
import jiangxiaopeng.ai.pet.application.dto.PetCreateCommand;
import jiangxiaopeng.ai.pet.application.dto.PetDetailDto;
import jiangxiaopeng.ai.pet.application.dto.PetSummaryDto;
import jiangxiaopeng.ai.pet.application.dto.PetUpdateCommand;
import jiangxiaopeng.ai.shared.infrastructure.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static jiangxiaopeng.ai.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

/**
 * 宠物档案 CRUD 接口（与 frontend-pet/backend_api.md §7 对齐）
 */
@Tag(name = "宠物", description = "宠物档案管理：列表、详情、创建、更新、删除")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    private final PetApplicationService petService;

    public PetController(PetApplicationService petService) {
        this.petService = petService;
    }

    @Operation(summary = "列出当前用户的宠物", description = "返回宠物摘要列表")
    @GetMapping
    public ApiResponse<List<PetSummaryDto>> listPets(@AuthenticationPrincipal UserPrincipal user) {
        Objects.requireNonNull(user, "user");
        return ApiResponse.ok(petService.listPets(user.getUid()));
    }

    @Operation(summary = "获取宠物详情", description = "根据宠物 ID 获取详细信息")
    @GetMapping("/{petId}")
    public ApiResponse<PetDetailDto> getPetDetail(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "宠物 ID") @PathVariable Long petId) {
        Objects.requireNonNull(user, "user");
        return ApiResponse.ok(petService.getPetDetail(petId, user.getUid()));
    }
    
    @Operation(summary = "创建宠物档案", description = "为当前用户创建新的宠物档案")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PetDetailDto> createPet(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody PetCreateCommand command) {
        Objects.requireNonNull(user, "user");
        return ApiResponse.ok(petService.createPet(command, user.getUid()));
    }

    @Operation(summary = "更新宠物档案", description = "更新指定宠物的档案信息")
    @PutMapping("/{petId}")
    public ApiResponse<PetDetailDto> updatePet(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @Valid @RequestBody PetUpdateCommand command) {
        Objects.requireNonNull(user, "user");
        return ApiResponse.ok(petService.updatePet(petId, command, user.getUid()));
    }

    @Operation(summary = "删除宠物档案", description = "软删除指定宠物的档案")
    @DeleteMapping("/{petId}")
    public ApiResponse<Map<String, Object>> deletePet(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "宠物 ID") @PathVariable Long petId) {
        Objects.requireNonNull(user, "user");
        petService.deletePet(petId, user.getUid());
        return ApiResponse.okEmpty();
    }

    @Operation(summary = "上传宠物头像", description = "上传宠物头像图片，支持 jpg/png/gif/webp，最大5MB")
    @PostMapping(value = "/{petId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PetAvatarUploadResponse> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @Parameter(description = "头像图片文件") @RequestParam("file") MultipartFile file) {
        Objects.requireNonNull(user, "user");
        return ApiResponse.ok(petService.uploadAvatar(petId, user.getUid(), file));
    }

    @Operation(summary = "获取头像上传预签名URL", description = "获取 OSS 直传的预签名 URL，前端使用此 URL 直接上传到 OSS")
    @GetMapping("/{petId}/avatar/presigned-url")
    public ApiResponse<PetAvatarPresignedUrlResponse> getAvatarUploadUrl(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @Parameter(description = "图片 Content-Type，如 image/jpeg") @RequestParam String contentType) {
        Objects.requireNonNull(user, "user");
        return ApiResponse.ok(petService.getAvatarUploadUrl(petId, user.getUid(), contentType));
    }

    @Operation(summary = "确认头像上传", description = "前端上传完成后调用此接口，确认上传成功并保存 avatarKey")
    @PostMapping("/{petId}/avatar/confirm")
    public ApiResponse<PetAvatarUploadResponse> confirmAvatarUpload(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "宠物 ID") @PathVariable Long petId,
            @Parameter(description = "上传后的 avatarKey") @RequestParam String avatarKey) {
        Objects.requireNonNull(user, "user");
        return ApiResponse.ok(petService.confirmAvatarUpload(petId, user.getUid(), avatarKey));
    }
}
