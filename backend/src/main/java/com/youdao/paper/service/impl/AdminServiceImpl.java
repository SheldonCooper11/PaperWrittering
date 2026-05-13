package com.youdao.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.dto.PasswordLoginRequest;
import com.youdao.paper.entity.PlatformPreset;
import com.youdao.paper.entity.Preset;
import com.youdao.paper.entity.RechargeRecord;
import com.youdao.paper.entity.RedeemCode;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.entity.SystemConfig;
import com.youdao.paper.entity.UserAccount;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.mapper.SysUserMapper;
import com.youdao.paper.service.AccountService;
import com.youdao.paper.service.AdminService;
import com.youdao.paper.service.AuthService;
import com.youdao.paper.service.ConfigService;
import com.youdao.paper.service.PresetService;
import com.youdao.paper.service.RedeemService;
import com.youdao.paper.vo.LoginVO;
import com.youdao.paper.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final SysUserMapper sysUserMapper;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final PresetService presetService;
    private final AccountService accountService;
    private final ConfigService configService;
    private final RedeemService redeemService;

    public AdminServiceImpl(SysUserMapper sysUserMapper, AuthService authService,
                            PasswordEncoder passwordEncoder, PresetService presetService,
                            AccountService accountService, ConfigService configService,
                            RedeemService redeemService) {
        this.sysUserMapper = sysUserMapper;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.presetService = presetService;
        this.accountService = accountService;
        this.configService = configService;
        this.redeemService = redeemService;
    }

    @Override
    public LoginVO login(PasswordLoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (user == null || !"ADMIN".equals(user.getRole()) || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }
        return authService.passwordLogin(request);
    }

    @Override
    public IPage<UserVO> users(long current, long size, String keyword) {
        Page<SysUser> page = sysUserMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<SysUser>()
                .ne(SysUser::getRole, "ADMIN")
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getPhone, keyword))
                .orderByDesc(SysUser::getCreateTime));
        Page<UserVO> result = Page.of(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toUserVO).toList());
        return result;
    }

    @Override
    public UserVO userDetail(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_ERROR, "用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || "ADMIN".equals(user.getRole())) {
            throw new BusinessException(ResultCode.USER_ERROR, "用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    @Override
    public List<Preset> presets() {
        return presetService.listAll();
    }

    @Override
    public Preset updatePreset(Long id, Preset preset) {
        return presetService.updatePreset(id, preset);
    }

    @Override
    public Preset createPreset(Preset preset) {
        return presetService.createPreset(preset);
    }

    @Override
    public void deletePreset(Long id) {
        presetService.deletePreset(id);
    }

    @Override
    public List<PlatformPreset> platformPresets() {
        return presetService.listPlatformPresets();
    }

    @Override
    public PlatformPreset createPlatformPreset(PlatformPreset mp) {
        return presetService.createPlatformPreset(mp);
    }

    @Override
    public PlatformPreset updatePlatformPreset(Long id, PlatformPreset mp) {
        return presetService.updatePlatformPreset(id, mp);
    }

    @Override
    public void deletePlatformPreset(Long id) {
        presetService.deletePlatformPreset(id);
    }

    @Override
    public RechargeRecord recharge(Long userId, BigDecimal amount, String remark, Long operatorId) {
        return accountService.recharge(userId, amount, remark, operatorId);
    }

    @Override
    public List<RechargeRecord> rechargeRecords(Long userId) {
        return accountService.rechargeRecords(userId);
    }

    @Override
    public List<SystemConfig> listConfigs() {
        return configService.listAll();
    }

    @Override
    public SystemConfig updateConfig(Long id, String value) {
        SystemConfig config = configService.listAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException(com.youdao.paper.common.ResultCode.PARAM_ERROR, "配置不存在"));
        return configService.set(config.getConfigKey(), value);
    }

    @Override
    public Page<RedeemCode> redeemCodes(int current, int size, String keyword) {
        return redeemService.list(current, size, keyword);
    }

    @Override
    public List<RedeemCode> generateRedeemCodes(BigDecimal amount, int count) {
        return redeemService.generate(amount, count);
    }

    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        UserAccount account = accountService.getOrCreateAccount(user.getId());
        vo.setBalance(account.getBalance());
        return vo;
    }
}
