package com.youdao.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.entity.ModulePlatform;
import com.youdao.paper.entity.Platform;
import com.youdao.paper.entity.PlatformPreset;
import com.youdao.paper.entity.Preset;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.mapper.ModulePlatformMapper;
import com.youdao.paper.mapper.PlatformMapper;
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
    private final PlatformMapper platformMapper;
    private final ModulePlatformMapper modulePlatformMapper;

    public PresetServiceImpl(PresetMapper presetMapper, PlatformPresetMapper platformPresetMapper,
                             PlatformMapper platformMapper, ModulePlatformMapper modulePlatformMapper) {
        this.presetMapper = presetMapper;
        this.platformPresetMapper = platformPresetMapper;
        this.platformMapper = platformMapper;
        this.modulePlatformMapper = modulePlatformMapper;
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
    public List<String> getPlatformsByModule(String moduleCode, String language) {
        // join module_platform + platform to get active platform names for this module+language
        List<ModulePlatform> mps = modulePlatformMapper.selectList(new LambdaQueryWrapper<ModulePlatform>()
                .eq(ModulePlatform::getModuleCode, moduleCode)
                .eq(ModulePlatform::getLanguage, language)
                .eq(ModulePlatform::getStatus, 1));
        if (mps.isEmpty()) return List.of();
        List<Long> ids = mps.stream().map(ModulePlatform::getPlatformId).toList();
        return platformMapper.selectBatchIds(ids).stream()
                .filter(p -> p.getStatus() == 1)
                .map(Platform::getName)
                .toList();
    }

    @Override
    public List<Preset> listAll() {
        return presetMapper.selectList(new LambdaQueryWrapper<Preset>()
                .orderByAsc(Preset::getSortOrder));
    }

    @Override
    public Preset updatePreset(Long id, Preset preset) {
        Preset exist = presetMapper.selectById(id);
        if (exist == null) throw new BusinessException(ResultCode.USER_ERROR, "预设不存在");
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
        if (presetMapper.exists(new LambdaQueryWrapper<Preset>().eq(Preset::getCode, preset.getCode())))
            throw new BusinessException(ResultCode.USER_ERROR, "预设代码已存在");
        presetMapper.insert(preset);
        return preset;
    }

    @Override
    public void deletePreset(Long id) {
        if (presetMapper.deleteById(id) <= 0)
            throw new BusinessException(ResultCode.USER_ERROR, "预设不存在");
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
                .eq(PlatformPreset::getPresetCode, pp.getPresetCode())))
            throw new BusinessException(ResultCode.USER_ERROR, "该预设已关联到此平台模块");
        platformPresetMapper.insert(pp);
        return pp;
    }

    @Override
    public PlatformPreset updatePlatformPreset(Long id, PlatformPreset pp) {
        PlatformPreset exist = platformPresetMapper.selectById(id);
        if (exist == null) throw new BusinessException(ResultCode.USER_ERROR, "关联不存在");
        exist.setSortOrder(pp.getSortOrder());
        exist.setStatus(pp.getStatus());
        platformPresetMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deletePlatformPreset(Long id) {
        if (platformPresetMapper.deleteById(id) <= 0)
            throw new BusinessException(ResultCode.USER_ERROR, "关联不存在");
    }

    @Override
    public List<Platform> listAllPlatforms() {
        return platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                .orderByAsc(Platform::getSortOrder));
    }

    @Override
    public Platform createPlatform(Platform platform) {
        if (platformMapper.exists(new LambdaQueryWrapper<Platform>()
                .eq(Platform::getName, platform.getName())))
            throw new BusinessException(ResultCode.USER_ERROR, "平台名称已存在");
        platformMapper.insert(platform);
        return platform;
    }

    @Override
    public Platform updatePlatform(Long id, Platform platform) {
        Platform exist = platformMapper.selectById(id);
        if (exist == null) throw new BusinessException(ResultCode.USER_ERROR, "平台不存在");
        exist.setName(platform.getName());
        exist.setSortOrder(platform.getSortOrder());
        exist.setStatus(platform.getStatus());
        platformMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deletePlatform(Long id) {
        if (platformMapper.deleteById(id) <= 0)
            throw new BusinessException(ResultCode.USER_ERROR, "平台不存在");
    }

    @Override
    public List<ModulePlatform> listModulePlatforms() {
        return modulePlatformMapper.selectList(new LambdaQueryWrapper<ModulePlatform>()
                .orderByAsc(ModulePlatform::getModuleCode, ModulePlatform::getLanguage, ModulePlatform::getSortOrder));
    }

    @Override
    public ModulePlatform createModulePlatform(ModulePlatform mp) {
        if (modulePlatformMapper.exists(new LambdaQueryWrapper<ModulePlatform>()
                .eq(ModulePlatform::getPlatformId, mp.getPlatformId())
                .eq(ModulePlatform::getModuleCode, mp.getModuleCode())
                .eq(ModulePlatform::getLanguage, mp.getLanguage())))
            throw new BusinessException(ResultCode.USER_ERROR, "该平台在此模块+语言下已存在");
        modulePlatformMapper.insert(mp);
        return mp;
    }

    @Override
    public ModulePlatform updateModulePlatform(Long id, ModulePlatform mp) {
        ModulePlatform exist = modulePlatformMapper.selectById(id);
        if (exist == null) throw new BusinessException(ResultCode.USER_ERROR, "关联不存在");
        exist.setSortOrder(mp.getSortOrder());
        exist.setStatus(mp.getStatus());
        modulePlatformMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deleteModulePlatform(Long id) {
        if (modulePlatformMapper.deleteById(id) <= 0)
            throw new BusinessException(ResultCode.USER_ERROR, "关联不存在");
    }
}
