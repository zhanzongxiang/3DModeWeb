package com.modelhub.backend.dto.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ModelUploadRequest {
    @NotBlank(message = "name required")
    @Size(max = 128, message = "name too long")
    private String name;

    @NotBlank(message = "artworkName required")
    @Size(max = 128, message = "artworkName too long")
    private String artworkName;

    @NotBlank(message = "type required")
    @Size(max = 64, message = "type too long")
    private String type;

    @NotBlank(message = "imageUrls required")
    private String imageUrls;

    @NotBlank(message = "diskLink required")
    private String diskLink;

    @NotNull(message = "isFree required")
    private Integer isFree;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtworkName() {
        return artworkName;
    }

    public void setArtworkName(String artworkName) {
        this.artworkName = artworkName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getDiskLink() {
        return diskLink;
    }

    public void setDiskLink(String diskLink) {
        this.diskLink = diskLink;
    }

    public Integer getIsFree() {
        return isFree;
    }

    public void setIsFree(Integer isFree) {
        this.isFree = isFree;
    }
}

