package com.modelhub.backend.service;

import com.modelhub.backend.entity.TbCollection;

import java.util.List;

public interface CollectionService {
    boolean toggle(Long userId, Long modelId);

    boolean isCollected(Long userId, Long modelId);

    List<TbCollection> listByUserId(Long userId);
}
