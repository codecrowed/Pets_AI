package jiangxiaopeng.ai.storage.domain.service;

import java.io.InputStream;
import java.net.URL;

public interface StorageService {
    void upload(String key, InputStream inputStream, String contentType, long size);
    InputStream download(String key);
    void delete(String key);
    boolean exists(String key);
    URL generatePresignedUploadUrl(String key, String contentType, long expirationSeconds);
    String getPublicUrl(String key);
}
