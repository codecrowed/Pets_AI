package jiangxiaopeng.ai.storage.infrastructure.oss;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import jiangxiaopeng.ai.storage.domain.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Component
public class OssStorageAdapter implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(OssStorageAdapter.class);

    private final OSS ossClient;
    private final String bucketName;
    private final String publicUrl;

    public OssStorageAdapter(OSS ossClient,
                             @Value("${aliyun.oss.bucket-name}") String bucketName,
                             @Value("${aliyun.oss.public-url:}") String publicUrl) {
        this.ossClient = ossClient;
        this.bucketName = bucketName;
        this.publicUrl = publicUrl;
    }

    @Override
    public void upload(String key, InputStream inputStream, String contentType, long size) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(size);
        ossClient.putObject(bucketName, key, inputStream, metadata);
    }

    @Override
    public InputStream download(String key) {
        return ossClient.getObject(bucketName, key).getObjectContent();
    }

    @Override
    public void delete(String key) {
        try {
            ossClient.deleteObject(bucketName, key);
        } catch (Exception e) {
            log.warn("Failed to delete file from OSS: {}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return ossClient.doesObjectExist(bucketName, key);
        } catch (Exception e) {
            log.warn("Failed to check if file exists in OSS: {}", key, e);
            return false;
        }
    }

    @Override
    public URL generatePresignedUploadUrl(String key, String contentType, long expirationSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expirationSeconds * 1000);
        
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key, HttpMethod.PUT);
        request.setExpiration(expiration);
        request.setContentType(contentType);
        
        return ossClient.generatePresignedUrl(request);
    }

    @Override
    public String getPublicUrl(String key) {
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl + "/" + key;
        }
        return "https://" + bucketName + ".oss-cn-shenzhen.aliyuncs.com/" + key;
    }
}
