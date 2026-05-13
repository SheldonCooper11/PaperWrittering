package com.youdao.paper.controller;

import com.youdao.paper.common.ResultVO;
import com.youdao.paper.dto.SendSmsRequest;
import com.youdao.paper.service.SmsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/send")
    public ResultVO<Void> send(@Valid @RequestBody SendSmsRequest request) {
        String scene = request.getScene() == null || request.getScene().isBlank() ? "login" : request.getScene();
        smsService.sendCode(request.getPhone(), scene);
        return ResultVO.success("验证码发送成功", null);
    }
}
