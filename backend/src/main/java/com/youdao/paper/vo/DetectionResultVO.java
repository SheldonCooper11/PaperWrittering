package com.youdao.paper.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DetectionResultVO {

    private Double score;
    private Double maxScore;
    private String level;
    private Boolean isHighRisk;
    private String error;
    private List<Segment> segments;
    private Integer totalChars;
    private BigDecimal userCost;
    private BigDecimal balanceBefore;
    private BigDecimal remainingBalance;

    private String fileName;
    private String language;
    private Map<String, String> reportFiles;

    @Data
    public static class Segment {
        private String segment;
        private String label;
    }
}
