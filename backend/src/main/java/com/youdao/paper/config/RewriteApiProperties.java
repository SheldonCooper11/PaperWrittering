package com.youdao.paper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rewrite-api")
public class RewriteApiProperties {

    private String apiKey;
    private String textUrl;
    private String documentUrl;
    private String defaultPresetEn = "aimove_weipu_english";
}
