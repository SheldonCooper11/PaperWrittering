package com.youdao.paper.controller;

import com.youdao.paper.common.ResultCode;
import com.youdao.paper.common.ResultVO;
import com.youdao.paper.entity.RedeemCode;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.service.RedeemService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/redeem")
public class RedeemController {

    private final RedeemService redeemService;

    public RedeemController(RedeemService redeemService) {
        this.redeemService = redeemService;
    }

    @PostMapping
    public ResultVO<RedeemCode> redeem(@RequestAttribute("currentUser") SysUser user,
                                       @RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请输入卡密");
        }
        RedeemCode result = redeemService.redeem(code.trim().toUpperCase(), user.getId());
        return ResultVO.success("兑换成功，获得 " + result.getAmount() + " 元余额", result);
    }
}
