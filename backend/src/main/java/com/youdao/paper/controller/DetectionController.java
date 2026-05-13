package com.youdao.paper.controller;

import com.youdao.paper.common.ResultVO;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.service.DetectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/detection")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping("/options")
    public ResultVO<Map<String, Object>> options(@RequestParam(defaultValue = "zh") String language) {
        return ResultVO.success(detectionService.getOptions(language));
    }

    @PostMapping("/check")
    public ResultVO<?> check(@RequestAttribute("currentUser") SysUser user,
                             @RequestBody Map<String, String> body) {
        String text = body.get("text");
        String taskPlatform = body.get("task_platform");
        if (text == null || text.isBlank()) {
            return ResultVO.fail("PARAM_ERROR", "文本不能为空");
        }
        if (taskPlatform == null || taskPlatform.isBlank()) {
            return ResultVO.fail("PARAM_ERROR", "请选择检测平台");
        }
        return ResultVO.success(detectionService.check(user, text, taskPlatform));
    }
}
