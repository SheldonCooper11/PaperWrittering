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
public class DetectionApiClient {

    private final RewriteApiProperties properties;

    public DetectionApiClient(RewriteApiProperties properties) {
        this.properties = properties;
    }

    public JSONObject getOptions(String language) {
        try {
            String url = properties.getDetectionUrl() + "/options?language=" + language;
            HttpResponse response = HttpRequest.get(url)
                    .header("Authorization", properties.getApiKey())
                    .timeout(30000)
                    .execute();
            String body = response.body();
            log.info("检测平台列表响应 status={}, body={}", response.getStatus(), body);
            if (!response.isOk()) {
                throw new BusinessException(ResultCode.THIRD_API_ERROR, "获取检测平台列表失败");
            }
            return JSONUtil.parseObj(body);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取检测平台列表异常", e);
            throw new BusinessException(ResultCode.THIRD_API_ERROR, "获取检测平台列表失败");
        }
    }

    public JSONObject check(String text, String taskPlatform) {
        JSONObject request = new JSONObject();
        request.set("text", text);
        request.set("task_platform", taskPlatform);
        try {
            HttpResponse response = HttpRequest.post(properties.getDetectionUrl() + "/check")
                    .header("Authorization", properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(request.toString())
                    .timeout(120000)
                    .execute();
            String body = response.body();
            log.info("AI检测响应 status={}, body={}", response.getStatus(), body);
            if (!response.isOk()) {
                throw new BusinessException(ResultCode.THIRD_API_ERROR, "AI检测接口调用失败");
            }
            return JSONUtil.parseObj(body);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI检测接口异常", e);
            throw new BusinessException(ResultCode.THIRD_API_ERROR, "AI检测接口调用失败");
        }
    }

    public JSONObject checkFile(MultipartFile multipartFile, String taskPlatform, String title, String author) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("detection-", "-" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);
            var req = HttpRequest.post(properties.getDetectionUrl() + "/check-file")
                    .header("Authorization", properties.getApiKey())
                    .form("file", tempFile)
                    .form("task_platform", taskPlatform);
            if (title != null && !title.isBlank()) req.form("title", title);
            if (author != null && !author.isBlank()) req.form("author", author);
            HttpResponse response = req.timeout(300000).execute();
            String body = response.body();
            log.info("文档AI检测响应 status={}, body={}", response.getStatus(), body);
            if (!response.isOk()) {
                throw new BusinessException(ResultCode.THIRD_API_ERROR, "文档AI检测接口调用失败");
            }
            return JSONUtil.parseObj(body);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文档AI检测接口异常", e);
            throw new BusinessException(ResultCode.THIRD_API_ERROR, "文档AI检测接口调用失败");
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("临时文件删除失败 file={}", tempFile.getAbsolutePath());
            }
        }
    }
}
