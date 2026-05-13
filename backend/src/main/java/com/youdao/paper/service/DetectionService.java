package com.youdao.paper.service;

import com.youdao.paper.entity.SysUser;
import com.youdao.paper.vo.DetectionResultVO;

import java.util.Map;

public interface DetectionService {

    /** 获取可用检测平台列表 */
    Map<String, Object> getOptions(String language);

    /** 执行AI检测 */
    DetectionResultVO check(SysUser user, String text, String taskPlatform);
}
