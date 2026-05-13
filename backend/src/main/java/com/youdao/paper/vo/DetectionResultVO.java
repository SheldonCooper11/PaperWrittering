package com.youdao.paper.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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

    @Data
    public static class Segment {
        private String segment;
        private String label;
    }
}
