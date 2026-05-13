package com.youdao.paper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("redeem_code")
public class RedeemCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private BigDecimal amount;

    private Integer status;

    private Long usedBy;

    private LocalDateTime usedAt;

    private String batchNo;

    private LocalDateTime createTime;
}
