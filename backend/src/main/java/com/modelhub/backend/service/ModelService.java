package com.modelhub.backend.service;

import com.modelhub.backend.dto.model.ModelListQuery;
import com.modelhub.backend.dto.model.ModelListResponse;
import com.modelhub.backend.dto.model.ModelUploadRequest;

public interface ModelService {
    void upload(Long userId, ModelUploadRequest request);

    ModelListResponse list(ModelListQuery query);
}

