package jiangxiaopeng.ai.storage.application.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import jiangxiaopeng.ai.storage.application.dto.FileUploadResponse;
import jiangxiaopeng.ai.storage.domain.model.Attachment;
import jiangxiaopeng.ai.storage.domain.model.FileMetadata;
import jiangxiaopeng.ai.storage.domain.model.StorageKey;
import jiangxiaopeng.ai.storage.domain.repository.AttachmentRepository;
import jiangxiaopeng.ai.storage.domain.service.FileValidator;
import jiangxiaopeng.ai.storage.domain.service.StorageService;

@Service
@Transactional
public class FileApplicationService {

    private final AttachmentRepository attachmentRepository;
    private final FileValidator fileValidator;
    private final StorageService storageService;

    public FileApplicationService(AttachmentRepository attachmentRepository,
                                  FileValidator fileValidator,
                                  StorageService storageService) {
        this.attachmentRepository = attachmentRepository;
        this.fileValidator = fileValidator;
        this.storageService = storageService;
    }

    public FileUploadResponse uploadFile(Long uid, MultipartFile file) {
        fileValidator.validate(file.getOriginalFilename(), file.getSize());

        String storageKey = "uploads/" + uid + "/" + UUID.randomUUID() + "/" + file.getOriginalFilename();

        try {
            storageService.upload(storageKey, file.getInputStream(), file.getContentType(), file.getSize());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.MSG_001, "文件上传失败: " + e.getMessage());
        }

        FileMetadata metadata = new FileMetadata(file.getOriginalFilename(), file.getContentType(), file.getSize());
        Attachment attachment = Attachment.create(uid, metadata, new StorageKey(storageKey));
        attachment = attachmentRepository.save(attachment);

        return new FileUploadResponse(
                attachment.getUid(),
                metadata.originalName(),
                metadata.contentType(),
                metadata.fileSize(),
                "READY"
        );
    }

    @Transactional(readOnly = true)
    public FileUploadResponse getFileInfo(Long fileId, Long userId) {
        Attachment attachment = attachmentRepository.findByFileIdUid(fileId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_003));
        return new FileUploadResponse(
                attachment.getUid(),
                attachment.getMetadata().originalName(),
                attachment.getMetadata().contentType(),
                attachment.getMetadata().fileSize(),
                "READY"
        );
    }

    public InputStream downloadFile(Long fileId, Long uid) {
        Attachment attachment = attachmentRepository.findByFileIdUid(fileId, uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_003));
        return storageService.download(attachment.getStorageKey().value());
    }

    public void deleteFile(Long fileId, Long uid) {
        Attachment attachment = attachmentRepository.findByFileIdUid(fileId, uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_003));
        storageService.delete(attachment.getStorageKey().value());
        attachmentRepository.deleteById(attachment.getId());
    }
}
