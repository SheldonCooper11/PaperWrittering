package com.youdao.paper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.youdao.paper.mapper")
public class PaperRewriteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperRewriteApplication.class, args);
    }
}
