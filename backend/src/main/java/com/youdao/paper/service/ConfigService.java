package com.youdao.paper.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youdao.paper.entity.SystemConfig;
import com.youdao.paper.mapper.SystemConfigMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public ConfigService(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
    }

    @PostConstruct
    public void loadAll() {
        List<SystemConfig> all = systemConfigMapper.selectList(new LambdaQueryWrapper<>());
        for (SystemConfig c : all) {
            cache.put(c.getConfigKey(), c.getConfigValue());
        }
        ensureDefaults();
    }

    public String get(String key) {
        return cache.get(key);
    }

    public BigDecimal getDecimal(String key) {
        String val = cache.get(key);
        return val == null ? BigDecimal.ZERO : new BigDecimal(val);
    }

    public List<SystemConfig> listAll() {
        return systemConfigMapper.selectList(new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getId));
    }

    public SystemConfig set(String key, String value) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            systemConfigMapper.insert(config);
        } else {
            config.setConfigValue(value);
            systemConfigMapper.updateById(config);
        }
        cache.put(key, value);
        return config;
    }

    private void ensureDefaults() {
        setIfMissing("min_balance", "1.0", "最低余额阈值");
        setIfMissing("price_per_kchars", "1.5", "每千字单价(元)-默认");
        setIfMissing("price_repeat", "1.5", "降重功能每千字单价(元)");
        setIfMissing("price_ai", "1.5", "降AI功能每千字单价(元)");
        setIfMissing("price_dual", "1.5", "双降功能每千字单价(元)");
    }

    private void setIfMissing(String key, String defaultVal, String desc) {
        if (cache.containsKey(key)) return;
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(defaultVal);
        config.setDescription(desc);
        systemConfigMapper.insert(config);
        cache.put(key, defaultVal);
    }
}
