package com.youdao.paper.service;

public interface SmsService {

    void sendCode(String phone, String scene);

    void verifyCode(String phone, String scene, String code);
}
