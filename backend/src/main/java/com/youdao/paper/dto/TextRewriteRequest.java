package com.youdao.paper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TextRewriteRequest {

    @NotBlank(message = "文本不能为空")
    private String text;

    @NotBlank(message = "预设不能为空")
    private String preset;

    private String language;
    private String module;
    private boolean free;
}
