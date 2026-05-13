package com.youdao.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.entity.RechargeRecord;
import com.youdao.paper.entity.UserAccount;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.mapper.RechargeRecordMapper;
import com.youdao.paper.mapper.UserAccountMapper;
import com.youdao.paper.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserAccountMapper userAccountMapper;
    private final RechargeRecordMapper rechargeRecordMapper;

    public AccountServiceImpl(UserAccountMapper userAccountMapper, RechargeRecordMapper rechargeRecordMapper) {
        this.userAccountMapper = userAccountMapper;
        this.rechargeRecordMapper = rechargeRecordMapper;
    }

    @Override
    public UserAccount getOrCreateAccount(Long userId) {
        UserAccount account = userAccountMapper.selectOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUserId, userId));
        if (account == null) {
            account = new UserAccount();
            account.setUserId(userId);
            account.setBalance(BigDecimal.ZERO);
            account.setTotalRecharged(BigDecimal.ZERO);
            account.setTotalConsumed(BigDecimal.ZERO);
            userAccountMapper.insert(account);
        }
        return account;
    }

    @Override
    @Transactional
    public UserAccount deductBalance(Long userId, BigDecimal amount) {
        UserAccount account = getOrCreateAccount(userId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.USER_ERROR, "余额不足，请联系客服充值");
        }
        account.setBalance(account.getBalance().subtract(amount));
        account.setTotalConsumed(account.getTotalConsumed().add(amount));
        userAccountMapper.updateById(account);
        return account;
    }

    @Override
    @Transactional
    public RechargeRecord recharge(Long userId, BigDecimal amount, String remark, Long operatorId) {
        UserAccount account = getOrCreateAccount(userId);
        BigDecimal before = account.getBalance();
        account.setBalance(before.add(amount));
        account.setTotalRecharged(account.getTotalRecharged().add(amount));
        userAccountMapper.updateById(account);

        RechargeRecord record = new RechargeRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setBalanceBefore(before);
        record.setBalanceAfter(account.getBalance());
        record.setRemark(remark);
        record.setOperatorId(operatorId);
        rechargeRecordMapper.insert(record);
        return record;
    }

    @Override
    public List<RechargeRecord> rechargeRecords(Long userId) {
        return rechargeRecordMapper.selectList(new LambdaQueryWrapper<RechargeRecord>()
                .eq(RechargeRecord::getUserId, userId)
                .orderByDesc(RechargeRecord::getCreateTime));
    }
}
