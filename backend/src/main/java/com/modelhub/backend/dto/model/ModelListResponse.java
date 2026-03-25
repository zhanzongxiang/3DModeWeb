package com.modelhub.backend.dto.model;

import com.modelhub.backend.entity.TbModel;

import java.util.List;

public class ModelListResponse {
    private List<TbModel> items;
    private long total;
    private long page;
    private long size;

    public ModelListResponse() {
    }

    public ModelListResponse(List<TbModel> items, long total, long page, long size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<TbModel> getItems() {
        return items;
    }

    public void setItems(List<TbModel> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}

