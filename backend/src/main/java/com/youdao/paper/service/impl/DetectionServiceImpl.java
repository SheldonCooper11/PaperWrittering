package com.youdao.paper.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.entity.UserAccount;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.service.AccountService;
import com.youdao.paper.service.ConfigService;
import com.youdao.paper.service.DetectionService;
import com.youdao.paper.util.DetectionApiClient;
import com.youdao.paper.vo.DetectionResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DetectionServiceImpl implements DetectionService {

    private final DetectionApiClient detectionApiClient;
    private final AccountService accountService;
    private final ConfigService configService;

    public DetectionServiceImpl(DetectionApiClient detectionApiClient,
                                AccountService accountService,
                                ConfigService configService) {
        this.detectionApiClient = detectionApiClient;
        this.accountService = accountService;
        this.configService = configService;
    }

    @Override
    public Map<String, Object> getOptions(String language) {
        JSONObject response = detectionApiClient.getOptions(language);
        Map<String, Object> result = new HashMap<>();
        JSONObject data = response.getJSONObject("data");
        if (data != null) {
            result.put("language", data.getStr("language"));
            JSONArray platforms = data.getJSONArray("platforms");
            result.put("platforms", platforms != null ? platforms.toList(Map.class) : List.of());
        }
        return result;
    }

    @Override
    public DetectionResultVO check(SysUser user, String text, String taskPlatform) {
        UserAccount account = accountService.getOrCreateAccount(user.getId());
        BigDecimal pricePerKChars = configService.getDecimal("price_per_kchars");

        // 预估费用
        int estimatedChars = text.replaceAll("\\s+", "").length();
        BigDecimal estimatedCost = BigDecimal.valueOf(estimatedChars)
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP)
                .multiply(pricePerKChars)
                .setScale(4, RoundingMode.HALF_UP);
        if (account.getBalance().compareTo(estimatedCost) < 0) {
            throw new BusinessException(ResultCode.USER_ERROR,
                    String.format("余额不足，预计扣费 %.4f 元，当前余额 %.4f 元，请充值", estimatedCost, account.getBalance()));
        }

        JSONObject response = detectionApiClient.check(text, taskPlatform);
        JSONObject data = response.getJSONObject("data");

        DetectionResultVO vo = new DetectionResultVO();
        if (data != null) {
            vo.setScore(data.getDouble("score"));
            vo.setMaxScore(data.getDouble("max_score"));
            vo.setLevel(data.getStr("level"));
            vo.setIsHighRisk(data.getBool("is_high_risk"));
            vo.setError(data.getStr("error"));
            vo.setTotalChars(data.getInt("total_chars"));
            vo.setSegments(parseSegments(data.getJSONArray("segments")));
        }

        // 用实际字符数计费
        Integer actualChars = vo.getTotalChars();
        int charCount = actualChars != null && actualChars > 0 ? actualChars : estimatedChars;
        BigDecimal userCost = BigDecimal.valueOf(charCount)
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP)
                .multiply(pricePerKChars)
                .setScale(4, RoundingMode.HALF_UP);
        vo.setUserCost(userCost);
        vo.setBalanceBefore(account.getBalance());

        UserAccount updated = accountService.deductBalance(user.getId(), userCost);
        vo.setRemainingBalance(updated.getBalance());

        log.info("[AI检测] 预估字符={}, 实际字符={}, 扣费={} 元, 检测等级={}",
                estimatedChars, charCount, userCost, vo.getLevel());

        return vo;
    }

    @Override
    public DetectionResultVO checkFile(SysUser user, MultipartFile file, String taskPlatform, String title, String author) {
        UserAccount account = accountService.getOrCreateAccount(user.getId());
        BigDecimal pricePerKChars = configService.getDecimal("price_per_kchars");

        // 预估费用（文档按1KB≈500字粗略估算，实际以API返回为准）
        int estimatedChars = Math.max((int)(file.getSize() / 2), 100);
        BigDecimal estimatedCost = BigDecimal.valueOf(estimatedChars)
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP)
                .multiply(pricePerKChars)
                .setScale(4, RoundingMode.HALF_UP);
        if (account.getBalance().compareTo(estimatedCost) < 0) {
            throw new BusinessException(ResultCode.USER_ERROR,
                    String.format("余额不足，预计扣费 %.4f 元，当前余额 %.4f 元，请充值", estimatedCost, account.getBalance()));
        }

        JSONObject response = detectionApiClient.checkFile(file, taskPlatform, title, author);
        JSONObject data = response.getJSONObject("data");

        DetectionResultVO vo = new DetectionResultVO();
        if (data != null) {
            vo.setScore(data.getDouble("score"));
            vo.setMaxScore(data.getDouble("max_score"));
            vo.setLevel(data.getStr("level"));
            vo.setIsHighRisk(data.getBool("is_high_risk"));
            vo.setError(data.getStr("error"));
            vo.setTotalChars(data.getInt("total_chars"));
            vo.setSegments(parseSegments(data.getJSONArray("segments")));
            vo.setFileName(data.getStr("file_name"));
            vo.setLanguage(data.getStr("language"));
            JSONObject reportFiles = data.getJSONObject("report_files");
            if (reportFiles != null) {
                Map<String, String> rf = new HashMap<>();
                rf.put("html_detail_url", reportFiles.getStr("html_detail_url"));
                rf.put("pdf_summary_url", reportFiles.getStr("pdf_summary_url"));
                vo.setReportFiles(rf);
            }
        }

        Integer actualChars = vo.getTotalChars();
        int charCount = actualChars != null && actualChars > 0 ? actualChars : estimatedChars;
        BigDecimal userCost = BigDecimal.valueOf(charCount)
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP)
                .multiply(pricePerKChars)
                .setScale(4, RoundingMode.HALF_UP);
        vo.setUserCost(userCost);
        vo.setBalanceBefore(account.getBalance());

        UserAccount updated = accountService.deductBalance(user.getId(), userCost);
        vo.setRemainingBalance(updated.getBalance());

        log.info("[AI文档检测] 文件={}, 字符数={}, 扣费={}元, 检测等级={}",
                file.getOriginalFilename(), charCount, userCost, vo.getLevel());

        return vo;
    }

    private List<DetectionResultVO.Segment> parseSegments(JSONArray arr) {
        if (arr == null || arr.isEmpty()) return List.of();
        List<DetectionResultVO.Segment> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject seg = arr.getJSONObject(i);
            DetectionResultVO.Segment s = new DetectionResultVO.Segment();
            s.setSegment(seg.getStr("segment"));
            s.setLabel(seg.getStr("label"));
            list.add(s);
        }
        return list;
    }
}
