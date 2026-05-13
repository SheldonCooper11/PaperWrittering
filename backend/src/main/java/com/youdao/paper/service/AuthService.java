package com.youdao.paper.service;

import com.youdao.paper.dto.PasswordLoginRequest;
import com.youdao.paper.dto.RegisterRequest;
import com.youdao.paper.dto.ResetPasswordRequest;
import com.youdao.paper.dto.SmsLoginRequest;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.vo.CheckVO;
import com.youdao.paper.vo.LoginVO;

public interface AuthService {

    CheckVO checkUsername(String username);

    CheckVO checkPhone(String phone);

    void register(RegisterRequest request);

    LoginVO passwordLogin(PasswordLoginRequest request);

    LoginVO smsLogin(SmsLoginRequest request);

    void resetPassword(ResetPasswordRequest request);

    SysUser getLoginUser(String token);

    LoginVO getUserInfoByUser(SysUser user);

    void changePassword(SysUser user, String oldPassword, String newPassword);
}
