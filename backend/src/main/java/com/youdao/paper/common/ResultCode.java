package com.youdao.paper.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS("00000", "请求成功"),
    PARAM_ERROR("A0400", "请求参数错误"),
    UNAUTHORIZED("A0401", "用户未登录"),
    FORBIDDEN("A0403", "无访问权限"),
    USER_ERROR("A1000", "用户业务错误"),
    VERIFY_CODE_ERROR("A1001", "验证码错误或已过期"),
    LOGIN_ERROR("A1002", "用户名或密码错误"),
    THIRD_API_ERROR("B2000", "第三方接口调用失败"),
    SYSTEM_ERROR("B5000", "系统异常");

    private final String code;
    private final String msg;

    ResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
