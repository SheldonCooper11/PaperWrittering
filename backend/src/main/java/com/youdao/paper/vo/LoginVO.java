package com.youdao.paper.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String phone;
    private String role;
    private BigDecimal balance;

    public LoginVO(String token, Long userId, String username, String phone, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.role = role;
    }
}
