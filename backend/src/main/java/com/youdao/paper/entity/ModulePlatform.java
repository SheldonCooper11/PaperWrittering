package com.youdao.paper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("module_platform")
public class ModulePlatform {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long platformId;

    private String moduleCode;

    private String moduleName;

    private String language;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
