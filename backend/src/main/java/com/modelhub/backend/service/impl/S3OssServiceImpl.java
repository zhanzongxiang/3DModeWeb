package com.modelhub.backend.service.impl;

import com.modelhub.backend.config.OssProperties;
import com.modelhub.backend.service.OssService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class S3OssServiceImpl implements OssService {
    private final S3Client s3Client;
    private final OssProperties ossProperties;

    public S3OssServiceImpl(S3Client s3Client, OssProperties ossProperties) {
        this.s3Client = s3Client;
        this.ossProperties = ossProperties;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new com.modelhub.backend.common.BusinessException(HttpStatus.BAD_REQUEST, "empty file");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new com.modelhub.backend.common.BusinessException(HttpStatus.BAD_REQUEST, "only image file is allowed");
        }

        String key = buildKey(file.getOriginalFilename());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(ossProperties.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return toPublicUrl(key);
        } catch (IOException e) {
            throw new com.modelhub.backend.common.BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "upload failed");
        }
    }

    private String buildKey(String fileName) {
        String suffix = ".bin";
        if (fileName != null) {
            int index = fileName.lastIndexOf('.');
            if (index > -1 && index < fileName.length() - 1) {
                suffix = fileName.substring(index);
            }
        }
        LocalDate now = LocalDate.now();
        return "model-images/" + now.getYear() + "/" + now.getMonthValue() + "/" + now.getDayOfMonth()
                + "/" + UUID.randomUUID() + suffix;
    }

    private String toPublicUrl(String key) {
        String base = ossProperties.getPublicBaseUrl();
        if (base != null && !base.isBlank()) {
            return trimRightSlash(base) + "/" + key;
        }
        return trimRightSlash(ossProperties.getEndpoint()) + "/" + ossProperties.getBucket() + "/" + key;
    }

    private String trimRightSlash(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.endsWith("/") ? raw.substring(0, raw.length() - 1) : raw;
    }
}

