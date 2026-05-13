package com.youdao.paper.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
public class DocumentCharCounter {

    /** 支持的格式 */
    public static boolean isSupported(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".docx") || lower.endsWith(".txt");
    }

    /** 统计不含空格的字符数 */
    public static int count(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return fallback(file.getSize());

        String lower = filename.toLowerCase();
        try {
            if (lower.endsWith(".docx")) {
                return stripWhitespace(countDocx(file));
            }
            if (lower.endsWith(".txt")) {
                return stripWhitespace(countTxt(file));
            }
        } catch (Exception e) {
            log.warn("解析文档字符数失败: {}，使用文件大小估算", filename, e);
        }
        return fallback(file.getSize());
    }

    private static String countDocx(MultipartFile file) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            doc.getParagraphs().forEach(p -> {
                String text = p.getText();
                if (text != null) sb.append(text);
            });
            doc.getTables().forEach(t ->
                t.getRows().forEach(r ->
                    r.getTableCells().forEach(c ->
                        c.getParagraphs().forEach(p -> {
                            String text = p.getText();
                            if (text != null) sb.append(text);
                        })
                    )
                )
            );
            return sb.toString();
        }
    }

    private static String countTxt(MultipartFile file) throws IOException {
        return new String(file.getBytes(), "UTF-8");
    }

    /** 去掉所有空白字符（空格、换行、制表符等） */
    private static int stripWhitespace(String text) {
        return text.replaceAll("\\s+", "").length();
    }

    private static int fallback(long fileSize) {
        return Math.max((int) (fileSize / 3), 1);
    }
}
