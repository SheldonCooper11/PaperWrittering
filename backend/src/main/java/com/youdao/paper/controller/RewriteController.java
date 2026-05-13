package com.youdao.paper.controller;

import com.youdao.paper.common.ResultVO;
import com.youdao.paper.dto.TextRewriteRequest;
import com.youdao.paper.entity.Preset;
import com.youdao.paper.entity.RewriteRecord;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.service.PresetService;
import com.youdao.paper.service.RewriteService;
import com.youdao.paper.vo.RewriteResultVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/rewrite")
public class RewriteController {

    private final RewriteService rewriteService;
    private final PresetService presetService;

    public RewriteController(RewriteService rewriteService, PresetService presetService) {
        this.rewriteService = rewriteService;
        this.presetService = presetService;
    }

    @PostMapping("/text")
    public ResultVO<RewriteResultVO> rewriteText(@RequestAttribute("currentUser") SysUser user,
                                                 @Valid @RequestBody TextRewriteRequest request) {
        return ResultVO.success(rewriteService.rewriteText(user, request));
    }

    @PostMapping("/document")
    public ResultVO<RewriteResultVO> rewriteDocument(@RequestAttribute("currentUser") SysUser user,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam String preset) {
        return ResultVO.success(rewriteService.rewriteDocument(user, file, preset));
    }

    @GetMapping("/records")
    public ResultVO<List<RewriteRecord>> records(@RequestAttribute("currentUser") SysUser user) {
        return ResultVO.success(rewriteService.records(user));
    }

    @GetMapping("/presets")
    public ResultVO<List<Preset>> presets(@RequestParam String module,
                                          @RequestParam String platform,
                                          @RequestParam(defaultValue = "chinese") String language) {
        return ResultVO.success(presetService.listByModule(module, platform, language));
    }
}
