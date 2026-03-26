package com.modelhub.backend.dto.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

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

    @Positive(message = "printLayerHeight should be positive")
    private BigDecimal printLayerHeight;

    @PositiveOrZero(message = "printInfill should be >= 0")
    private Integer printInfill;

    @Min(value = 0, message = "printSupport should be 0 or 1")
    @Max(value = 1, message = "printSupport should be 0 or 1")
    private Integer printSupport;

    @Size(max = 32, message = "printMaterial too long")
    private String printMaterial;

    @Size(max = 32, message = "licenseType too long")
    private String licenseType;

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

    public BigDecimal getPrintLayerHeight() {
        return printLayerHeight;
    }

    public void setPrintLayerHeight(BigDecimal printLayerHeight) {
        this.printLayerHeight = printLayerHeight;
    }

    public Integer getPrintInfill() {
        return printInfill;
    }

    public void setPrintInfill(Integer printInfill) {
        this.printInfill = printInfill;
    }

    public Integer getPrintSupport() {
        return printSupport;
    }

    public void setPrintSupport(Integer printSupport) {
        this.printSupport = printSupport;
    }

    public String getPrintMaterial() {
        return printMaterial;
    }

    public void setPrintMaterial(String printMaterial) {
        this.printMaterial = printMaterial;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }
}
