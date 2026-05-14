package com.youdao.paper.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RewriteResultVO {

    private Boolean success;
    private String originalText;
    private String paraphrasedText;
    private String message;
    private String presetUsed;
    private String presetName;
    private Double processingTime;
    private Integer characterCount;
    private Integer chineseCharacters;
    private Integer englishWords;
    private String detectedLanguage;
    private BigDecimal cost;
    private BigDecimal remainingBalance;
    private String requestId;
    private String fileId;
    private String originalFilename;
    private String originalOssUrl;
    private String paraphrasedOssUrl;
    private String paraphrasedFilename;
    private String diffUrl;
    private Integer totalCharacters;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private Double originalAiRate;
    private Double rewrittenAiRate;
    private Double reduction;
    private Boolean aiDetectionSuccess;
    private Long recordId;
}
