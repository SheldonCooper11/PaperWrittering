package com.youdao.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.dto.PasswordLoginRequest;
import com.youdao.paper.dto.RegisterRequest;
import com.youdao.paper.dto.ResetPasswordRequest;
import com.youdao.paper.dto.SmsLoginRequest;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.entity.UserAccount;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.mapper.SysUserMapper;
import com.youdao.paper.service.AccountService;
import com.youdao.paper.service.AuthService;
import com.youdao.paper.service.SmsService;
import com.youdao.paper.vo.CheckVO;
import com.youdao.paper.vo.LoginVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    private static final String TOKEN_PREFIX = "login:token:";

    private final SysUserMapper sysUserMapper;
    private final SmsService smsService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    public AuthServiceImpl(SysUserMapper sysUserMapper, SmsService smsService, StringRedisTemplate redisTemplate,
                           PasswordEncoder passwordEncoder, AccountService accountService) {
        this.sysUserMapper = sysUserMapper;
        this.smsService = smsService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }

    @Override
    public CheckVO checkUsername(String username) {
        return new CheckVO(countByUsername(username) == 0);
    }

    @Override
    public CheckVO checkPhone(String phone) {
        return new CheckVO(countByPhone(phone) == 0);
    }

    @Override
    public void register(RegisterRequest request) {
        validatePassword(request.getPassword());
        if (countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(ResultCode.USER_ERROR, "用户名已存在");
        }
        if (countByPhone(request.getPhone()) > 0) {
            throw new BusinessException(ResultCode.USER_ERROR, "手机号已存在");
        }
        smsService.verifyCode(request.getPhone(), "register", request.getCode());
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);
    }

    @Override
    public LoginVO passwordLogin(PasswordLoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }
        checkUserEnabled(user);
        return buildLogin(user);
    }

    @Override
    public LoginVO smsLogin(SmsLoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, request.getPhone())
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_ERROR, "手机号未注册");
        }
        smsService.verifyCode(request.getPhone(), "login", request.getCode());
        checkUserEnabled(user);
        return buildLogin(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        validatePassword(request.getNewPassword());
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, request.getPhone())
                .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_ERROR, "手机号不存在");
        }
        smsService.verifyCode(request.getPhone(), "reset", request.getCode());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    public SysUser getLoginUser(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String userId = redisTemplate.opsForValue().get(TOKEN_PREFIX + token.replace("Bearer ", ""));
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        SysUser user = sysUserMapper.selectById(Long.valueOf(userId));
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        checkUserEnabled(user);
        return user;
    }

    @Override
    public LoginVO getUserInfoByUser(SysUser user) {
        UserAccount account = accountService.getOrCreateAccount(user.getId());
        LoginVO vo = new LoginVO(null, user.getId(), user.getUsername(), user.getPhone(), user.getRole());
        vo.setBalance(account.getBalance());
        return vo;
    }

    @Override
    public void changePassword(SysUser user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_ERROR, "旧密码不正确");
        }
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }

    private LoginVO buildLogin(SysUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, String.valueOf(user.getId()), Duration.ofHours(12));
        UserAccount account = accountService.getOrCreateAccount(user.getId());
        LoginVO vo = new LoginVO(token, user.getId(), user.getUsername(), user.getPhone(), user.getRole());
        vo.setBalance(account.getBalance());
        return vo;
    }

    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码必须8位及以上，并且同时包含字母和数字");
        }
    }

    private void checkUserEnabled(SysUser user) {
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_ERROR, "账号已被禁用");
        }
    }

    private Long countByUsername(String username) {
        return sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    private Long countByPhone(String phone) {
        return sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
    }
}
