package com.youdao.paper.service;

import com.youdao.paper.entity.PlatformPreset;
import com.youdao.paper.entity.Preset;

import java.util.List;

public interface PresetService {

    List<Preset> listByModule(String moduleCode, String platform, String language);

    List<Preset> listAll();

    Preset updatePreset(Long id, Preset preset);

    Preset createPreset(Preset preset);

    void deletePreset(Long id);

    List<PlatformPreset> listPlatformPresets();

    PlatformPreset createPlatformPreset(PlatformPreset pp);

    PlatformPreset updatePlatformPreset(Long id, PlatformPreset pp);

    void deletePlatformPreset(Long id);
}
