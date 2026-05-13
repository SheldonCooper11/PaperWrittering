package com.youdao.paper.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youdao.paper.common.ResultVO;
import com.youdao.paper.dto.PasswordLoginRequest;
import com.youdao.paper.entity.Announcement;
import com.youdao.paper.entity.ModulePlatform;
import com.youdao.paper.entity.Platform;
import com.youdao.paper.entity.PlatformPreset;
import com.youdao.paper.entity.Preset;
import com.youdao.paper.entity.RechargeRecord;
import com.youdao.paper.entity.RedeemCode;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.entity.SystemConfig;
import com.youdao.paper.service.AdminService;
import com.youdao.paper.vo.LoginVO;
import com.youdao.paper.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody PasswordLoginRequest request) {
        return ResultVO.success(adminService.login(request));
    }

    @GetMapping("/users")
    public ResultVO<IPage<UserVO>> users(@RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String keyword) {
        return ResultVO.success(adminService.users(current, size, keyword));
    }

    @GetMapping("/users/{id}")
    public ResultVO<UserVO> detail(@PathVariable Long id) {
        return ResultVO.success(adminService.userDetail(id));
    }

    @PutMapping("/users/{id}/status")
    public ResultVO<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateStatus(id, status);
        return ResultVO.success("操作成功", null);
    }

    @PostMapping("/users/{id}/recharge")
    public ResultVO<RechargeRecord> recharge(@RequestAttribute("currentUser") SysUser operator,
                                             @PathVariable Long id,
                                             @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        String remark = (String) body.getOrDefault("remark", "");
        return ResultVO.success(adminService.recharge(id, amount, remark, operator.getId()));
    }

    @GetMapping("/users/{id}/recharge-records")
    public ResultVO<List<RechargeRecord>> rechargeRecords(@PathVariable Long id) {
        return ResultVO.success(adminService.rechargeRecords(id));
    }

    @GetMapping("/configs")
    public ResultVO<List<SystemConfig>> configs() {
        return ResultVO.success(adminService.listConfigs());
    }

    @PutMapping("/configs/{id}")
    public ResultVO<SystemConfig> updateConfig(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body) {
        return ResultVO.success(adminService.updateConfig(id, String.valueOf(body.get("configValue"))));
    }

    @GetMapping("/redeem-codes")
    public ResultVO<Page<RedeemCode>> redeemCodes(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResultVO.success(adminService.redeemCodes(current, size, keyword));
    }

    @PostMapping("/redeem-codes/generate")
    public ResultVO<List<RedeemCode>> generateRedeemCodes(@RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
        int count = Integer.parseInt(String.valueOf(body.get("count")));
        return ResultVO.success(adminService.generateRedeemCodes(amount, count));
    }

    @GetMapping("/presets")
    public ResultVO<List<Preset>> presets() {
        return ResultVO.success(adminService.presets());
    }

    @PutMapping("/presets/{id}")
    public ResultVO<Preset> updatePreset(@PathVariable Long id, @RequestBody Preset preset) {
        return ResultVO.success(adminService.updatePreset(id, preset));
    }

    @PostMapping("/presets")
    public ResultVO<Preset> createPreset(@RequestBody Preset preset) {
        return ResultVO.success(adminService.createPreset(preset));
    }

    @DeleteMapping("/presets/{id}")
    public ResultVO<Void> deletePreset(@PathVariable Long id) {
        adminService.deletePreset(id);
        return ResultVO.success("删除成功", null);
    }

    @GetMapping("/platform-presets")
    public ResultVO<List<PlatformPreset>> platformPresets() {
        return ResultVO.success(adminService.platformPresets());
    }

    @PostMapping("/platform-presets")
    public ResultVO<PlatformPreset> createPlatformPreset(@RequestBody PlatformPreset mp) {
        return ResultVO.success(adminService.createPlatformPreset(mp));
    }

    @PutMapping("/platform-presets/{id}")
    public ResultVO<PlatformPreset> updatePlatformPreset(@PathVariable Long id, @RequestBody PlatformPreset mp) {
        return ResultVO.success(adminService.updatePlatformPreset(id, mp));
    }

    @DeleteMapping("/platform-presets/{id}")
    public ResultVO<Void> deletePlatformPreset(@PathVariable Long id) {
        adminService.deletePlatformPreset(id);
        return ResultVO.success("删除成功", null);
    }

    @GetMapping("/announcements")
    public ResultVO<List<Announcement>> announcements() {
        return ResultVO.success(adminService.listAnnouncements());
    }

    @PostMapping("/announcements")
    public ResultVO<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        return ResultVO.success(adminService.createAnnouncement(announcement));
    }

    @PutMapping("/announcements/{id}")
    public ResultVO<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        return ResultVO.success(adminService.updateAnnouncement(id, announcement));
    }

    @DeleteMapping("/announcements/{id}")
    public ResultVO<Void> deleteAnnouncement(@PathVariable Long id) {
        adminService.deleteAnnouncement(id);
        return ResultVO.success("删除成功", null);
    }

    @GetMapping("/platforms")
    public ResultVO<List<Platform>> platforms() {
        return ResultVO.success(adminService.listPlatforms());
    }

    @PostMapping("/platforms")
    public ResultVO<Platform> createPlatform(@RequestBody Platform platform) {
        return ResultVO.success(adminService.createPlatform(platform));
    }

    @PutMapping("/platforms/{id}")
    public ResultVO<Platform> updatePlatform(@PathVariable Long id, @RequestBody Platform platform) {
        return ResultVO.success(adminService.updatePlatform(id, platform));
    }

    @DeleteMapping("/platforms/{id}")
    public ResultVO<Void> deletePlatform(@PathVariable Long id) {
        adminService.deletePlatform(id);
        return ResultVO.success("删除成功", null);
    }

    @GetMapping("/module-platforms")
    public ResultVO<List<ModulePlatform>> modulePlatforms() {
        return ResultVO.success(adminService.listModulePlatforms());
    }

    @PostMapping("/module-platforms")
    public ResultVO<ModulePlatform> createModulePlatform(@RequestBody ModulePlatform mp) {
        return ResultVO.success(adminService.createModulePlatform(mp));
    }

    @PutMapping("/module-platforms/{id}")
    public ResultVO<ModulePlatform> updateModulePlatform(@PathVariable Long id, @RequestBody ModulePlatform mp) {
        return ResultVO.success(adminService.updateModulePlatform(id, mp));
    }

    @DeleteMapping("/module-platforms/{id}")
    public ResultVO<Void> deleteModulePlatform(@PathVariable Long id) {
        adminService.deleteModulePlatform(id);
        return ResultVO.success("删除成功", null);
    }
}
