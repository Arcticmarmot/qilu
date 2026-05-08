package com.marmot.qilu.modules.media.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.storage.StorageProperties;
import com.marmot.qilu.modules.media.service.MediaService;
import com.marmot.qilu.modules.media.vo.MediaUploadVO;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;

    @Override
    public MediaUploadVO uploadPostImage(MultipartFile file) {
        validateImageFile(file);
        String currUserUuid = UserContext.requireUuid();

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();

        String objectKey = buildObjectKey(currUserUuid, originalFilename);
        String url = buildPublicUrl(objectKey);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(objectKey)
                            .stream(file.getInputStream(), size, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("upload image success, userUuid={}, objectKey={}, size={}", currUserUuid, objectKey, size);
            return new MediaUploadVO(
                    objectKey,
                    url,
                    originalFilename,
                    contentType,
                    size
            );
        } catch (Exception e) {
            log.error("upload image failed, userUuid={}, objectKey={}", currUserUuid, objectKey, e);
            throw new IllegalStateException("upload image failed");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("file is empty");
        }

        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new BadRequestException("file size exceeds limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BadRequestException("file type is not supported");
        }
    }

    private String buildObjectKey(String userUuid, String originalFilename) {
        LocalDate now = LocalDate.now();
        String extension = getExtension(originalFilename);
        String filename = UUID.randomUUID() + extension;

        return String.format(
                "%s/%d/%02d/%02d/%s/%s",
                storageProperties.getPostImagePrefix(),
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                userUuid,
                filename
        );
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }

        int index = originalFilename.lastIndexOf(".");
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }

        String extension = originalFilename.substring(index).toLowerCase();

        return switch (extension) {
            case ".jpg", ".jpeg", ".png", ".webp" -> extension;
            default -> "";
        };
    }

    private String buildPublicUrl(String objectKey) {
        return storageProperties.getPublicEndpoint()
                + "/"
                + storageProperties.getBucket()
                + "/"
                + objectKey;
    }
}
