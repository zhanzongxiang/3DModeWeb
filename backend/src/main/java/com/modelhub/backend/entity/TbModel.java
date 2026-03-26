package com.modelhub.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("tb_model")
public class TbModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String artworkName;
    private String type;
    private String imageUrls;
    private String diskLink;
    private Integer isFree;
    private BigDecimal printLayerHeight;
    private Integer printInfill;
    private Integer printSupport;
    private String printMaterial;
    private String licenseType;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
