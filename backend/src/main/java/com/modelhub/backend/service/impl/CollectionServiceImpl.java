package com.modelhub.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.modelhub.backend.entity.TbCollection;
import com.modelhub.backend.mapper.TbCollectionMapper;
import com.modelhub.backend.service.CollectionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final TbCollectionMapper collectionMapper;

    public CollectionServiceImpl(TbCollectionMapper collectionMapper) {
        this.collectionMapper = collectionMapper;
    }

    @Override
    public boolean toggle(Long userId, Long modelId) {
        TbCollection existing = collectionMapper.selectOne(
                new LambdaQueryWrapper<TbCollection>()
                        .eq(TbCollection::getUserId, userId)
                        .eq(TbCollection::getModelId, modelId)
        );
        if (existing != null) {
            collectionMapper.deleteById(existing.getId());
            return false;
        }

        TbCollection row = new TbCollection();
        row.setUserId(userId);
        row.setModelId(modelId);
        row.setCreateTime(LocalDateTime.now());
        collectionMapper.insert(row);
        return true;
    }

    @Override
    public boolean isCollected(Long userId, Long modelId) {
        Long count = collectionMapper.selectCount(
                new LambdaQueryWrapper<TbCollection>()
                        .eq(TbCollection::getUserId, userId)
                        .eq(TbCollection::getModelId, modelId)
        );
        return count != null && count > 0;
    }

    @Override
    public List<TbCollection> listByUserId(Long userId) {
        return collectionMapper.selectList(
                new LambdaQueryWrapper<TbCollection>()
                        .eq(TbCollection::getUserId, userId)
                        .orderByDesc(TbCollection::getCreateTime)
        );
    }
}
