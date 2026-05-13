package com.youdao.paper.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rewrite_record")
public class RewriteRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String rewriteType;

    private String language;

    private String platform;

    private String rewriteMode;

    private String originalText;

    private String paraphrasedText;

    private String originalFileUrl;

    private String paraphrasedFileUrl;

    private String originalFilename;

    private String paraphrasedFilename;

    private Integer totalCharacters;

    private BigDecimal cost;

    private String apiResponse;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
