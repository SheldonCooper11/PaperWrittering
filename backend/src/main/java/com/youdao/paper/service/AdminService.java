package com.youdao.paper.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youdao.paper.dto.PasswordLoginRequest;
import com.youdao.paper.entity.Announcement;
import com.youdao.paper.entity.ModulePlatform;
import com.youdao.paper.entity.Platform;
import com.youdao.paper.entity.PlatformPreset;
import com.youdao.paper.entity.Preset;
import com.youdao.paper.entity.RechargeRecord;
import com.youdao.paper.entity.RedeemCode;
import com.youdao.paper.entity.SystemConfig;
import com.youdao.paper.vo.LoginVO;
import com.youdao.paper.vo.UserVO;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {

    LoginVO login(PasswordLoginRequest request);

    IPage<UserVO> users(long current, long size, String keyword);

    UserVO userDetail(Long id);

    void updateStatus(Long id, Integer status);

    RechargeRecord recharge(Long userId, BigDecimal amount, String remark, Long operatorId);

    List<RechargeRecord> rechargeRecords(Long userId);

    List<SystemConfig> listConfigs();

    SystemConfig updateConfig(Long id, String value);

    Page<RedeemCode> redeemCodes(int current, int size, String keyword);

    List<RedeemCode> generateRedeemCodes(BigDecimal amount, int count);

    List<Preset> presets();

    Preset updatePreset(Long id, Preset preset);

    Preset createPreset(Preset preset);

    void deletePreset(Long id);

    List<PlatformPreset> platformPresets();

    PlatformPreset createPlatformPreset(PlatformPreset pp);

    PlatformPreset updatePlatformPreset(Long id, PlatformPreset pp);

    void deletePlatformPreset(Long id);

    List<Platform> listPlatforms();

    Platform createPlatform(Platform platform);

    Platform updatePlatform(Long id, Platform platform);

    void deletePlatform(Long id);

    List<ModulePlatform> listModulePlatforms();

    ModulePlatform createModulePlatform(ModulePlatform mp);

    ModulePlatform updateModulePlatform(Long id, ModulePlatform mp);

    void deleteModulePlatform(Long id);

    List<Announcement> listAnnouncements();

    Announcement createAnnouncement(Announcement announcement);

    Announcement updateAnnouncement(Long id, Announcement announcement);

    void deleteAnnouncement(Long id);
}
