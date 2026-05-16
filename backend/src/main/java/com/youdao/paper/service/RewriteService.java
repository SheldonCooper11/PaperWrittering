package com.youdao.paper.service;

import com.youdao.paper.dto.TextRewriteRequest;
import com.youdao.paper.entity.RewriteRecord;
import com.youdao.paper.entity.SysUser;
import com.youdao.paper.vo.RewriteResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface RewriteService {

    RewriteResultVO rewriteText(SysUser user, TextRewriteRequest request);

    Map<String, Object> precheckDocument(SysUser user, MultipartFile file, String module);

    RewriteResultVO rewriteDocument(SysUser user, MultipartFile file, String preset, String language, String presetName, String module);

    List<RewriteRecord> records(SysUser user);

    byte[] downloadRecord(SysUser user, Long recordId, String type);
}
