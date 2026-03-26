package com.modelhub.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.modelhub.backend.dto.make.MakeCreateRequest;
import com.modelhub.backend.dto.make.MakeView;
import com.modelhub.backend.entity.TbMake;
import com.modelhub.backend.mapper.TbMakeMapper;
import com.modelhub.backend.service.MakeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MakeServiceImpl implements MakeService {
    private final TbMakeMapper makeMapper;

    public MakeServiceImpl(TbMakeMapper makeMapper) {
        this.makeMapper = makeMapper;
    }

    @Override
    public void create(Long userId, MakeCreateRequest request) {
        TbMake make = new TbMake();
        make.setModelId(request.getModelId());
        make.setUserId(userId);
        make.setImageUrl(request.getImageUrl());
        make.setDescription(request.getDescription());
        make.setCreateTime(LocalDateTime.now());
        makeMapper.insert(make);
    }

    @Override
    public List<MakeView> listByModelId(Long modelId) {
        List<TbMake> rows = makeMapper.selectList(
                new LambdaQueryWrapper<TbMake>()
                        .eq(TbMake::getModelId, modelId)
                        .orderByDesc(TbMake::getCreateTime)
        );
        return rows.stream().map(this::toView).collect(Collectors.toList());
    }

    private MakeView toView(TbMake make) {
        MakeView view = new MakeView();
        view.setId(make.getId());
        view.setModelId(make.getModelId());
        view.setUserId(make.getUserId());
        view.setImageUrl(make.getImageUrl());
        view.setDescription(make.getDescription());
        view.setCreateTime(make.getCreateTime());
        return view;
    }
}

