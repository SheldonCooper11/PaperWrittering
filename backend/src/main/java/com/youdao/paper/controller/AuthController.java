package com.youdao.paper.controller;

import com.youdao.paper.common.ResultVO;
import com.youdao.paper.dto.ChangePasswordRequest;
import com.youdao.paper.dto.PasswordLoginRequest;
import com.youdao.paper.dto.RegisterRequest;
import com.youdao.paper.dto.ResetPasswordRequest;
import com.youdao.paper.dto.SmsLoginRequest;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.service.AuthService;
import com.youdao.paper.vo.CheckVO;
import com.youdao.paper.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResultVO<LoginVO> me(@RequestAttribute("currentUser") SysUser user) {
        return ResultVO.success(authService.getUserInfoByUser(user));
    }

    @PutMapping("/password")
    public ResultVO<Void> changePassword(@RequestAttribute("currentUser") SysUser user,
                                         @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user, request.getOldPassword(), request.getNewPassword());
        return ResultVO.success("密码修改成功", null);
    }

    @GetMapping("/check-username")
    public ResultVO<CheckVO> checkUsername(@RequestParam String username) {
        return ResultVO.success(authService.checkUsername(username));
    }

    @GetMapping("/check-phone")
    public ResultVO<CheckVO> checkPhone(@RequestParam String phone) {
        return ResultVO.success(authService.checkPhone(phone));
    }

    @PostMapping("/register")
    public ResultVO<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResultVO.success("注册成功", null);
    }

    @PostMapping("/login/password")
    public ResultVO<LoginVO> passwordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return ResultVO.success(authService.passwordLogin(request));
    }

    @PostMapping("/login/sms")
    public ResultVO<LoginVO> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return ResultVO.success(authService.smsLogin(request));
    }

    @PostMapping("/password/reset")
    public ResultVO<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResultVO.success("密码重置成功", null);
    }
}
