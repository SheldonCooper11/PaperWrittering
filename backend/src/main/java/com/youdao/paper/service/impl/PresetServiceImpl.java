package com.youdao.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.entity.PlatformPreset;
import com.youdao.paper.entity.Preset;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.mapper.PlatformPresetMapper;
import com.youdao.paper.mapper.PresetMapper;
import com.youdao.paper.service.PresetService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PresetServiceImpl implements PresetService {

    private final PresetMapper presetMapper;
    private final PlatformPresetMapper platformPresetMapper;

    public PresetServiceImpl(PresetMapper presetMapper, PlatformPresetMapper platformPresetMapper) {
        this.presetMapper = presetMapper;
        this.platformPresetMapper = platformPresetMapper;
    }

    @Override
    public List<Preset> listByModule(String moduleCode, String platform, String language) {
        List<PlatformPreset> mappings = platformPresetMapper.selectList(new LambdaQueryWrapper<PlatformPreset>()
                .eq(PlatformPreset::getModuleCode, moduleCode)
                .eq(PlatformPreset::getPlatform, platform)
                .eq(PlatformPreset::getLanguage, language)
                .eq(PlatformPreset::getStatus, 1)
                .orderByAsc(PlatformPreset::getSortOrder));
        if (mappings.isEmpty()) return List.of();
        List<String> codes = mappings.stream().map(PlatformPreset::getPresetCode).toList();
        Map<String, Preset> presetMap = presetMapper.selectList(new LambdaQueryWrapper<Preset>()
                        .in(Preset::getCode, codes)
                        .eq(Preset::getStatus, 1))
                .stream().collect(Collectors.toMap(Preset::getCode, p -> p));
        List<Preset> result = new ArrayList<>();
        for (PlatformPreset pp : mappings) {
            Preset p = presetMap.get(pp.getPresetCode());
            if (p != null) result.add(p);
        }
        return result;
    }

    @Override
    public List<Preset> listAll() {
        return presetMapper.selectList(new LambdaQueryWrapper<Preset>()
                .orderByAsc(Preset::getSortOrder));
    }

    @Override
    public Preset updatePreset(Long id, Preset preset) {
        Preset exist = presetMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_ERROR, "预设不存在");
        }
        exist.setName(preset.getName());
        exist.setCategory(preset.getCategory());
        exist.setPrice(preset.getPrice());
        exist.setStatus(preset.getStatus());
        exist.setSortOrder(preset.getSortOrder());
        presetMapper.updateById(exist);
        return exist;
    }

    @Override
    public Preset createPreset(Preset preset) {
        if (presetMapper.exists(new LambdaQueryWrapper<Preset>().eq(Preset::getCode, preset.getCode()))) {
            throw new BusinessException(ResultCode.USER_ERROR, "预设代码已存在");
        }
        presetMapper.insert(preset);
        return preset;
    }

    @Override
    public void deletePreset(Long id) {
        if (presetMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.USER_ERROR, "预设不存在");
        }
    }

    @Override
    public List<PlatformPreset> listPlatformPresets() {
        return platformPresetMapper.selectList(new LambdaQueryWrapper<PlatformPreset>()
                .orderByAsc(PlatformPreset::getPlatform, PlatformPreset::getModuleCode, PlatformPreset::getSortOrder));
    }

    @Override
    public PlatformPreset createPlatformPreset(PlatformPreset pp) {
        if (platformPresetMapper.exists(new LambdaQueryWrapper<PlatformPreset>()
                .eq(PlatformPreset::getPlatform, pp.getPlatform())
                .eq(PlatformPreset::getModuleCode, pp.getModuleCode())
                .eq(PlatformPreset::getLanguage, pp.getLanguage())
                .eq(PlatformPreset::getPresetCode, pp.getPresetCode()))) {
            throw new BusinessException(ResultCode.USER_ERROR, "该预设已关联到此平台模块");
        }
        platformPresetMapper.insert(pp);
        return pp;
    }

    @Override
    public PlatformPreset updatePlatformPreset(Long id, PlatformPreset pp) {
        PlatformPreset exist = platformPresetMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_ERROR, "关联不存在");
        }
        exist.setSortOrder(pp.getSortOrder());
        exist.setStatus(pp.getStatus());
        platformPresetMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deletePlatformPreset(Long id) {
        if (platformPresetMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.USER_ERROR, "关联不存在");
        }
    }
}
