package com.youdao.paper.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.config.RewriteApiProperties;
import com.youdao.paper.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Component
public class RewriteApiClient {

    private final RewriteApiProperties properties;

    public RewriteApiClient(RewriteApiProperties properties) {
        this.properties = properties;
    }

    public JSONObject paraphraseText(String text, String preset) {
        JSONObject request = new JSONObject();
        request.set("text", text);
        request.set("preset", preset);
        try {
            HttpResponse response = HttpRequest.post(properties.getTextUrl())
                    .header("Authorization", properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(request.toString())
                    .timeout(0)
                    .execute();
            String body = response.body();
            log.info("文本改写接口响应 status={}, body={}", response.getStatus(), body);
            if (!response.isOk()) {
                throw new BusinessException(ResultCode.THIRD_API_ERROR, "文本改写接口调用失败");
            }
            return JSONUtil.parseObj(body);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文本改写接口异常", e);
            throw new BusinessException(ResultCode.THIRD_API_ERROR, "文本改写接口调用失败");
        }
    }

    public JSONObject paraphraseDocument(MultipartFile multipartFile, String presetZh, String presetEn) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("rewrite-", "-" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);
            HttpResponse response = HttpRequest.post(properties.getDocumentUrl())
                    .header("Authorization", properties.getApiKey())
                    .form("file", tempFile)
                    .form("preset_zh", presetZh)
                    .form("preset_en", presetEn)
                    .form("generate_diff", "false")
                    .timeout(0)
                    .execute();
            String body = response.body();
            log.info("文件改写接口响应 status={}, body={}", response.getStatus(), body);
            if (!response.isOk()) {
                throw new BusinessException(ResultCode.THIRD_API_ERROR, "文件改写接口调用失败");
            }
            return JSONUtil.parseObj(body);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件改写接口异常", e);
            throw new BusinessException(ResultCode.THIRD_API_ERROR, "文件改写接口调用失败");
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("临时文件删除失败 file={}", tempFile.getAbsolutePath());
            }
        }
    }
}
