package com.modelhub.backend.service;

import com.modelhub.backend.dto.make.MakeCreateRequest;
import com.modelhub.backend.dto.make.MakeView;

import java.util.List;

public interface MakeService {
    void create(Long userId, MakeCreateRequest request);

    List<MakeView> listByModelId(Long modelId);
}

