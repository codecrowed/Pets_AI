package jiangxiaopeng.ai.storage.domain.service;

import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FileValidator {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "txt", "pdf", "png", "jpg", "jpeg", "csv", "json", "md",
            "py", "java", "js", "ts", "doc", "docx"
    );

    public void validate(String filename, long fileSize) {
        if (fileSize > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_001);
        }

        String extension = getExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_002);
        }
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) return "";
        return filename.substring(lastDot + 1);
    }
}
