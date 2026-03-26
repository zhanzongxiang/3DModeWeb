package com.modelhub.backend.dto.make;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MakeCreateRequest {
    @NotNull(message = "modelId required")
    private Long modelId;

    @NotBlank(message = "imageUrl required")
    @Size(max = 512, message = "imageUrl too long")
    private String imageUrl;

    @Size(max = 1000, message = "description too long")
    private String description;

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

