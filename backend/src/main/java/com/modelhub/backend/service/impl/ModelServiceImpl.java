package com.modelhub.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelhub.backend.dto.model.ModelListQuery;
import com.modelhub.backend.dto.model.ModelListResponse;
import com.modelhub.backend.dto.model.ModelUploadRequest;
import com.modelhub.backend.entity.TbModel;
import com.modelhub.backend.mapper.TbModelMapper;
import com.modelhub.backend.service.ModelService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ModelServiceImpl implements ModelService {
    private static final String VERSION_KEY = "models:list:version";

    private final TbModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ModelServiceImpl(
            TbModelMapper modelMapper,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.modelMapper = modelMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void upload(Long userId, ModelUploadRequest request) {
        TbModel model = new TbModel();
        model.setUserId(userId);
        model.setName(request.getName());
        model.setArtworkName(request.getArtworkName());
        model.setType(request.getType());
        model.setImageUrls(request.getImageUrls());
        model.setDiskLink(request.getDiskLink());
        model.setIsFree(request.getIsFree());
        model.setCreateTime(LocalDateTime.now());
        modelMapper.insert(model);

        redisTemplate.opsForValue().increment(VERSION_KEY);
    }

    @Override
    public ModelListResponse list(ModelListQuery query) {
        normalizeQuery(query);
        boolean shouldCache = query.getPage() == 1;
        String cacheKey = null;
        if (shouldCache) {
            cacheKey = cacheKey(query);
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof String) {
                String json = (String) cached;
                try {
                    return objectMapper.readValue(json, ModelListResponse.class);
                } catch (JsonProcessingException ignored) {
                    redisTemplate.delete(cacheKey);
                }
            }
        }

        LambdaQueryWrapper<TbModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getType()), TbModel::getType, query.getType())
                .like(StringUtils.hasText(query.getName()), TbModel::getName, query.getName())
                .like(StringUtils.hasText(query.getArtworkName()), TbModel::getArtworkName, query.getArtworkName())
                .orderByDesc(TbModel::getCreateTime);

        Page<TbModel> page = modelMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
        ModelListResponse response = new ModelListResponse(page.getRecords(), page.getTotal(), query.getPage(), query.getSize());

        if (shouldCache && cacheKey != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(response), Duration.ofSeconds(60));
            } catch (JsonProcessingException ignored) {
            }
        }
        return response;
    }

    private void normalizeQuery(ModelListQuery query) {
        if (query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getSize() < 1) {
            query.setSize(20);
        }
        if (query.getSize() > 50) {
            query.setSize(50);
        }
    }

    private String cacheKey(ModelListQuery query) {
        Object versionObj = redisTemplate.opsForValue().get(VERSION_KEY);
        long version = 1L;
        if (versionObj instanceof Number) {
            version = ((Number) versionObj).longValue();
        }
        return "models:list:v:" + version
                + ":page:" + query.getPage()
                + ":size:" + query.getSize()
                + ":type:" + safe(query.getType())
                + ":name:" + safe(query.getName())
                + ":artwork:" + safe(query.getArtworkName());
    }

    private String safe(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
