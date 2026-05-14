package com.youdao.paper.controller;

import com.youdao.paper.common.ResultCode;
import com.youdao.paper.common.ResultVO;
import com.youdao.paper.dto.TextRewriteRequest;
import com.youdao.paper.entity.Preset;
import com.youdao.paper.entity.RewriteRecord;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.service.PresetService;
import com.youdao.paper.service.RewriteService;
import com.youdao.paper.util.DocumentCharCounter;
import com.youdao.paper.vo.RewriteResultVO;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/document/precheck")
    public ResultVO<Map<String, Object>> precheckDocument(@RequestAttribute("currentUser") SysUser user,
                                                          @RequestParam("file") MultipartFile file) {
        if (!DocumentCharCounter.isSupported(file)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持 .docx 和 .txt 格式");
        }
        return ResultVO.success(rewriteService.precheckDocument(user, file));
    }

    @PostMapping("/document")
    public ResultVO<RewriteResultVO> rewriteDocument(@RequestAttribute("currentUser") SysUser user,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam String preset,
                                                     @RequestParam String language,
                                                     @RequestParam String presetName) {
        if (!DocumentCharCounter.isSupported(file)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持 .docx 和 .txt 格式");
        }
        return ResultVO.success(rewriteService.rewriteDocument(user, file, preset, language, presetName));
    }

    @GetMapping("/records")
    public ResultVO<List<RewriteRecord>> records(@RequestAttribute("currentUser") SysUser user) {
        return ResultVO.success(rewriteService.records(user));
    }

    @GetMapping("/records/{id}/download")
    public ResponseEntity<byte[]> downloadRecord(@RequestAttribute("currentUser") SysUser user,
                                                 @PathVariable Long id,
                                                 @RequestParam(defaultValue = "result") String type) {
        byte[] docx = rewriteService.downloadRecord(user, id, type);
        String filename = "original".equals(type) ? "原文.docx" : "改写结果.docx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docx);
    }

    @GetMapping("/presets")
    public ResultVO<List<Preset>> presets(@RequestParam String module,
                                          @RequestParam String platform,
                                          @RequestParam(defaultValue = "chinese") String language) {
        return ResultVO.success(presetService.listByModule(module, platform, language));
    }

    @GetMapping("/platforms")
    public ResultVO<List<String>> platforms(@RequestParam String module,
                                            @RequestParam(defaultValue = "chinese") String language) {
        return ResultVO.success(presetService.getPlatformsByModule(module, language));
    }
}
