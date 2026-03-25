package com.modelhub.backend.dto.oss;

public class OssUploadResponse {
    private String url;

    public OssUploadResponse() {
    }

    public OssUploadResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

