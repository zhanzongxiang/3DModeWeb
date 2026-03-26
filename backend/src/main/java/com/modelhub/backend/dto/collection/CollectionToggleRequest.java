package com.modelhub.backend.dto.collection;

import jakarta.validation.constraints.NotNull;

public class CollectionToggleRequest {
    @NotNull(message = "modelId required")
    private Long modelId;

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }
}

