package com.youdao.paper.service.impl;

import cn.hutool.http.HttpRequest;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.service.SmsService;
import com.youdao.paper.util.CodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private static final String CODE_PREFIX = "sms:code:";
    private static final String LIMIT_PREFIX = "sms:limit:";

    private final StringRedisTemplate redisTemplate;

    @Value("${sms.url}")
    private String smsUrl;

    public SmsServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void sendCode(String phone, String scene) {
        String limitKey = LIMIT_PREFIX + scene + ":" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new BusinessException(ResultCode.USER_ERROR, "同一手机号30秒内只能发送一次验证码");
        }
        String code = CodeUtil.sixDigitCode();
        String body = String.format("{\"name\":\"推送助手\",\"code\":\"%s\",\"to\":\"%s\"}", code, phone);
        try {
            String response = HttpRequest.post(smsUrl)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .timeout(10000)
                    .execute()
                    .body();
            log.info("短信验证码发送完成 phone={}, scene={}, response={}", phone, scene, response);
        } catch (Exception e) {
            log.error("短信验证码发送失败 phone={}, scene={}", phone, scene, e);
            throw new BusinessException(ResultCode.THIRD_API_ERROR, "短信发送失败");
        }
        redisTemplate.opsForValue().set(CODE_PREFIX + scene + ":" + phone, code, Duration.ofMinutes(1));
        redisTemplate.opsForValue().set(limitKey, "1", Duration.ofSeconds(30));
    }

    @Override
    public void verifyCode(String phone, String scene, String code) {
        String key = CODE_PREFIX + scene + ":" + phone;
        String cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }
        redisTemplate.delete(key);
    }
}
