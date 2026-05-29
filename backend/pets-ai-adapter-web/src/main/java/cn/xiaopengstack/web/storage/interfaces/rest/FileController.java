package jiangxiaopeng.ai.storage.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jiangxiaopeng.domain.identity.infrastructure.security.UserPrincipal;
import jiangxiaopeng.domain.shared.infrastructure.web.ApiResponse;
import jiangxiaopeng.domain.storage.application.dto.FileUploadResponse;
import jiangxiaopeng.domain.storage.application.service.FileApplicationService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static jiangxiaopeng.domain.shared.infrastructure.openapi.OpenApiConfig.SECURITY_SCHEME_BEARER_JWT;

import java.io.InputStream;
import java.util.Map;

@Tag(name = "文件", description = "文件上传、元信息、下载与删除")
@SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileApplicationService fileService;

    public FileController(FileApplicationService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "上传文件", description = "multipart/form-data，字段名为 file。")
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(fileService.uploadFile(user.getUid(), file));
    }

    @Operation(summary = "文件元信息", description = "返回文件名、类型、大小等。")
    @GetMapping("/{fileId}")
    public ApiResponse<FileUploadResponse> getFileInfo(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "文件 ID") @PathVariable Long fileId) {
        return ApiResponse.ok(fileService.getFileInfo(fileId, user.getUid()));
    }

    /** 流式下载需自定义头与 body，仍使用 {@link ResponseEntity}。 */
    @Operation(summary = "下载文件", description = "以附件形式流式返回文件内容。")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "文件 ID") @PathVariable Long fileId) {
        FileUploadResponse info = fileService.getFileInfo(fileId, user.getUid());
        InputStream stream = fileService.downloadFile(fileId, user.getUid());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(info.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + info.name() + "\"")
                .body(new InputStreamResource(stream));
    }

    @Operation(summary = "删除文件", description = "删除存储中的对象及元数据。")
    @DeleteMapping("/{fileId}")
    public ApiResponse<Map<String, Object>> deleteFile(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "文件 ID") @PathVariable Long fileId) {
        fileService.deleteFile(fileId, user.getUid());
        return ApiResponse.okEmpty();
    }
}
