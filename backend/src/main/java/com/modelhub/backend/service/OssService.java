package com.modelhub.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    String uploadImage(MultipartFile file);
}

