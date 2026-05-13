package com.youdao.paper.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.entity.RedeemCode;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.mapper.RedeemCodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class RedeemService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedeemCodeMapper redeemCodeMapper;
    private final AccountService accountService;

    public RedeemService(RedeemCodeMapper redeemCodeMapper, AccountService accountService) {
        this.redeemCodeMapper = redeemCodeMapper;
        this.accountService = accountService;
    }

    @Transactional
    public RedeemCode redeem(String code, Long userId) {
        RedeemCode redeemCode = redeemCodeMapper.selectOne(
                new LambdaQueryWrapper<RedeemCode>().eq(RedeemCode::getCode, code));
        if (redeemCode == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "卡密不存在");
        }
        if (redeemCode.getStatus() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该卡密已被使用");
        }
        redeemCode.setStatus(1);
        redeemCode.setUsedBy(userId);
        redeemCode.setUsedAt(LocalDateTime.now());
        redeemCodeMapper.updateById(redeemCode);

        accountService.recharge(userId, redeemCode.getAmount(), "卡密兑换：" + code, null);
        return redeemCode;
    }

    @Transactional
    public List<RedeemCode> generate(BigDecimal amount, int count) {
        String batchNo = "B" + System.currentTimeMillis();
        List<RedeemCode> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RedeemCode rc = new RedeemCode();
            rc.setCode(generateCode());
            rc.setAmount(amount);
            rc.setStatus(0);
            rc.setBatchNo(batchNo);
            redeemCodeMapper.insert(rc);
            list.add(rc);
        }
        return list;
    }

    private String generateCode() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).toUpperCase();
    }

    public Page<RedeemCode> list(int current, int size, String keyword) {
        LambdaQueryWrapper<RedeemCode> wrapper = new LambdaQueryWrapper<RedeemCode>()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(RedeemCode::getCode, keyword)
                        .or()
                        .like(RedeemCode::getBatchNo, keyword))
                .orderByDesc(RedeemCode::getCreateTime);
        return redeemCodeMapper.selectPage(Page.of(current, size), wrapper);
    }
}
