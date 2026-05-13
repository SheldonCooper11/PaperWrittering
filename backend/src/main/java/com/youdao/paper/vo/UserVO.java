package com.youdao.paper.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String phone;
    private String role;
    private Integer status;
    private BigDecimal balance;
    private LocalDateTime createTime;
}
