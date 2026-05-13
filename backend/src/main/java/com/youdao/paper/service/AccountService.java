package com.youdao.paper.service;

import com.youdao.paper.entity.RechargeRecord;
import com.youdao.paper.entity.UserAccount;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    UserAccount getOrCreateAccount(Long userId);

    UserAccount deductBalance(Long userId, BigDecimal amount);

    RechargeRecord recharge(Long userId, BigDecimal amount, String remark, Long operatorId);

    List<RechargeRecord> rechargeRecords(Long userId);
}
