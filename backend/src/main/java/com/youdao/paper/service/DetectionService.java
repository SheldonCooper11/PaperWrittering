package com.youdao.paper.service;

import com.youdao.paper.entity.SysUser;
import com.youdao.paper.vo.DetectionResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface DetectionService {

    /** 获取可用检测平台列表 */
    Map<String, Object> getOptions(String language);

    /** 执行AI文本检测 */
    DetectionResultVO check(SysUser user, String text, String taskPlatform);

    /** 执行AI文档检测 */
    DetectionResultVO checkFile(SysUser user, MultipartFile file, String taskPlatform, String title, String author);
}
