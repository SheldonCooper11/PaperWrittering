package com.youdao.paper.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.config.RewriteApiProperties;
import com.youdao.paper.dto.TextRewriteRequest;
import com.youdao.paper.entity.RewriteRecord;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.entity.UserAccount;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.mapper.RewriteRecordMapper;
import com.youdao.paper.service.AccountService;
import com.youdao.paper.service.ConfigService;
import com.youdao.paper.service.RewriteService;
import com.youdao.paper.util.RewriteApiClient;
import com.youdao.paper.vo.RewriteResultVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RewriteServiceImpl implements RewriteService {

    private final RewriteApiClient rewriteApiClient;
    private final RewriteRecordMapper rewriteRecordMapper;
    private final RewriteApiProperties rewriteApiProperties;
    private final AccountService accountService;
    private final ConfigService configService;

    public RewriteServiceImpl(RewriteApiClient rewriteApiClient,
                              RewriteRecordMapper rewriteRecordMapper,
                              RewriteApiProperties rewriteApiProperties,
                              AccountService accountService,
                              ConfigService configService) {
        this.rewriteApiClient = rewriteApiClient;
        this.rewriteRecordMapper = rewriteRecordMapper;
        this.rewriteApiProperties = rewriteApiProperties;
        this.accountService = accountService;
        this.configService = configService;
    }

    @Override
    public RewriteResultVO rewriteText(SysUser user, TextRewriteRequest request) {
        UserAccount account = accountService.getOrCreateAccount(user.getId());
        BigDecimal minBalance = configService.getDecimal("min_balance");
        if (account.getBalance().compareTo(minBalance) < 0) {
            throw new BusinessException(ResultCode.USER_ERROR, "余额不足，请联系客服充值");
        }
        JSONObject response = rewriteApiClient.paraphraseText(request.getText(), request.getPreset());
        RewriteResultVO result = mapResult(response);
        BigDecimal apiCost = toBigDecimal(response, "cost");
        if (apiCost == null) apiCost = BigDecimal.ZERO;
        BigDecimal userCost = apiCost.multiply(configService.getDecimal("price_markup"))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal balanceBefore = account.getBalance();
        UserAccount updated = accountService.deductBalance(user.getId(), userCost);
        RewriteRecord record = new RewriteRecord();
        record.setUserId(user.getId());
        record.setRewriteType("TEXT");
        record.setRewriteMode(response.getStr("preset_used"));
        record.setOriginalText(response.getStr("original_text", request.getText()));
        record.setParaphrasedText(response.getStr("paraphrased_text"));
        record.setTotalCharacters(response.getInt("characters_count"));
        record.setCost(userCost);
        record.setApiResponse(response.toString());
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(updated.getBalance());
        rewriteRecordMapper.insert(record);
        result.setCost(userCost);
        result.setRemainingBalance(updated.getBalance());
        return result;
    }

    @Override
    public RewriteResultVO rewriteDocument(SysUser user, MultipartFile file, String preset) {
        UserAccount account = accountService.getOrCreateAccount(user.getId());
        BigDecimal minBalance = configService.getDecimal("min_balance");
        if (account.getBalance().compareTo(minBalance) < 0) {
            throw new BusinessException(ResultCode.USER_ERROR, "余额不足，请联系客服充值");
        }
        JSONObject response = rewriteApiClient.paraphraseDocument(file, preset, rewriteApiProperties.getDefaultPresetEn());
        RewriteResultVO result = mapResult(response);
        BigDecimal apiCost = firstNonNullBigDecimal(response, "actual_cost", "cost");
        if (apiCost == null) apiCost = BigDecimal.ZERO;
        BigDecimal userCost = apiCost.multiply(configService.getDecimal("price_markup"))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal balanceBefore = account.getBalance();
        UserAccount updated = accountService.deductBalance(user.getId(), userCost);
        RewriteRecord record = new RewriteRecord();
        record.setUserId(user.getId());
        record.setRewriteType("DOCUMENT");
        record.setRewriteMode(response.getStr("preset_used"));
        record.setOriginalFilename(response.getStr("original_filename"));
        record.setParaphrasedFilename(extractFilename(response.getStr("paraphrased_url")));
        record.setOriginalFileUrl(response.getStr("original_url"));
        record.setParaphrasedFileUrl(response.getStr("paraphrased_url"));
        record.setTotalCharacters(response.getInt("total_characters"));
        record.setCost(userCost);
        record.setApiResponse(response.toString());
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(updated.getBalance());
        rewriteRecordMapper.insert(record);
        result.setCost(userCost);
        result.setActualCost(userCost);
        result.setRemainingBalance(updated.getBalance());
        return result;
    }

    @Override
    public List<RewriteRecord> records(SysUser user) {
        return rewriteRecordMapper.selectList(new LambdaQueryWrapper<RewriteRecord>()
                .eq(RewriteRecord::getUserId, user.getId())
                .orderByDesc(RewriteRecord::getCreateTime));
    }

    private RewriteResultVO mapResult(JSONObject response) {
        RewriteResultVO result = new RewriteResultVO();
        result.setSuccess(response.getBool("success"));
        result.setOriginalText(response.getStr("original_text"));
        result.setParaphrasedText(response.getStr("paraphrased_text"));
        result.setMessage(response.getStr("message"));
        result.setPresetUsed(response.getStr("preset_used"));
        result.setPresetName(response.getStr("preset_name"));
        result.setProcessingTime(response.getDouble("processing_time"));
        result.setCharacterCount(response.getInt("characters_count"));
        result.setDetectedLanguage(response.getStr("language_detected"));
        result.setCost(toBigDecimal(response, "cost"));
        result.setRemainingBalance(toBigDecimal(response, "remaining_balance"));
        result.setRequestId(response.getStr("request_id"));
        result.setFileId(response.getStr("file_id"));
        result.setOriginalFilename(response.getStr("original_filename"));
        result.setOriginalOssUrl(response.getStr("original_url"));
        result.setParaphrasedOssUrl(firstNonNull(response, "paraphrased_url", "rewritten_oss_url"));
        result.setParaphrasedFilename(firstNonNull(response, "paraphrased_filename", "rewritten_filename"));
        result.setDiffUrl(response.getStr("diff_url"));
        result.setTotalCharacters(response.getInt("total_characters"));
        result.setEstimatedCost(toBigDecimal(response, "estimated_cost"));
        result.setActualCost(toBigDecimal(response, "actual_cost"));
        result.setOriginalAiRate(response.getDouble("original_ai_rate"));
        result.setRewrittenAiRate(response.getDouble("rewritten_ai_rate"));
        result.setReduction(response.getDouble("reduction"));
        result.setAiDetectionSuccess(response.getBool("ai_detection_success"));
        return result;
    }

    private String firstNonNull(JSONObject response, String key1, String key2) {
        String val = response.getStr(key1);
        return val != null ? val : response.getStr(key2);
    }

    private BigDecimal firstNonNullBigDecimal(JSONObject response, String key1, String key2) {
        Object val = response.get(key1);
        if (val == null) val = response.get(key2);
        return val == null ? null : new BigDecimal(String.valueOf(val));
    }

    private String extractFilename(String url) {
        if (url == null) return null;
        String name = url.substring(url.lastIndexOf('/') + 1);
        int queryIdx = name.indexOf('?');
        if (queryIdx > 0) name = name.substring(0, queryIdx);
        try {
            name = java.net.URLDecoder.decode(name, "UTF-8");
        } catch (Exception ignored) {}
        return name.length() > 255 ? name.substring(0, 255) : name;
    }

    private BigDecimal toBigDecimal(JSONObject response, String key) {
        Object value = response.get(key);
        return value == null ? null : new BigDecimal(String.valueOf(value));
    }
}
