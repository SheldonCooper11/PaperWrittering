package com.youdao.paper.service;

import com.youdao.paper.entity.ModulePlatform;
import com.youdao.paper.entity.Platform;
import com.youdao.paper.entity.PlatformPreset;
import com.youdao.paper.entity.Preset;

import java.util.List;

public interface PresetService {

    List<Preset> listByModule(String moduleCode, String platform, String language);

    List<String> getPlatformsByModule(String moduleCode, String language);

    List<Preset> listAll();

    Preset updatePreset(Long id, Preset preset);

    Preset createPreset(Preset preset);

    void deletePreset(Long id);

    List<PlatformPreset> listPlatformPresets();

    PlatformPreset createPlatformPreset(PlatformPreset pp);

    PlatformPreset updatePlatformPreset(Long id, PlatformPreset pp);

    void deletePlatformPreset(Long id);

    // platform CRUD (names only)
    List<Platform> listAllPlatforms();

    Platform createPlatform(Platform platform);

    Platform updatePlatform(Long id, Platform platform);

    void deletePlatform(Long id);

    // module_platform CRUD (platform-to-module assignment)
    List<ModulePlatform> listModulePlatforms();

    ModulePlatform createModulePlatform(ModulePlatform mp);

    ModulePlatform updateModulePlatform(Long id, ModulePlatform mp);

    void deleteModulePlatform(Long id);
}
