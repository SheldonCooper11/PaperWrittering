CREATE DATABASE IF NOT EXISTS paper_rewrite DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE paper_rewrite;

DROP TABLE IF EXISTS redeem_code;
DROP TABLE IF EXISTS recharge_record;
DROP TABLE IF EXISTS user_account;
DROP TABLE IF EXISTS platform_preset;
DROP TABLE IF EXISTS module_platform;
DROP TABLE IF EXISTS preset;
DROP TABLE IF EXISTS platform;
DROP TABLE IF EXISTS rewrite_record;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码BCrypt摘要',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色 ADMIN/USER',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    KEY idx_role (role),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE rewrite_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    rewrite_type VARCHAR(20) NOT NULL COMMENT '改写类型 TEXT/DOCUMENT',
    language VARCHAR(20) DEFAULT NULL COMMENT '语言 chinese/english',
    platform VARCHAR(50) DEFAULT NULL COMMENT '平台',
    rewrite_mode VARCHAR(50) DEFAULT NULL COMMENT '改写模式',
    original_text LONGTEXT DEFAULT NULL COMMENT '原文',
    paraphrased_text LONGTEXT DEFAULT NULL COMMENT '改写结果',
    original_file_url VARCHAR(1000) DEFAULT NULL COMMENT '原文件地址',
    paraphrased_file_url VARCHAR(1000) DEFAULT NULL COMMENT '改写后文件地址',
    original_filename VARCHAR(255) DEFAULT NULL COMMENT '原文件名',
    paraphrased_filename VARCHAR(255) DEFAULT NULL COMMENT '改写后文件名',
    total_characters INT DEFAULT NULL COMMENT '总字符数',
    cost DECIMAL(12,4) DEFAULT NULL COMMENT '消耗',
    api_response JSON DEFAULT NULL COMMENT '第三方接口响应',
    balance_before DECIMAL(12,4) DEFAULT NULL COMMENT '扣费前余额',
    balance_after DECIMAL(12,4) DEFAULT NULL COMMENT '扣费后余额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_rewrite_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='改写记录表';

DROP TABLE IF EXISTS system_config;

CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    description VARCHAR(200) DEFAULT NULL COMMENT '说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

INSERT INTO system_config (config_key, config_value, description) VALUES
('min_balance', '1.0', '最低余额(元)：用户余额低于此值时无法改写'),
('price_per_kchars', '1.5', '每千字单价(元)-默认'),
('price_repeat', '1.5', '降重功能每千字单价(元)'),
('price_ai', '1.5', '降AI功能每千字单价(元)'),
('price_dual', '1.5', '双降功能每千字单价(元)');

CREATE TABLE user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    balance DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '当前余额(元)',
    total_recharged DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '累计充值',
    total_consumed DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '累计消费',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账户表';

CREATE TABLE recharge_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(12,4) NOT NULL COMMENT '充值金额',
    balance_before DECIMAL(12,4) NOT NULL COMMENT '充值前余额',
    balance_after DECIMAL(12,4) NOT NULL COMMENT '充值后余额',
    remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
    operator_id BIGINT DEFAULT NULL COMMENT '操作管理员ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

DROP TABLE IF EXISTS redeem_code;

CREATE TABLE redeem_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '卡密',
    amount DECIMAL(12,4) NOT NULL COMMENT '面额(元)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0未使用 1已使用',
    used_by BIGINT DEFAULT NULL COMMENT '使用者用户ID',
    used_at DATETIME DEFAULT NULL COMMENT '使用时间',
    batch_no VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_code (code),
    KEY idx_status (status),
    KEY idx_batch_no (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡密表';

DROP TABLE IF EXISTS preset;

CREATE TABLE platform (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '平台名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台字典表';

INSERT INTO platform (name, sort_order, status) VALUES
('知网',1,1),('维普',2,1),('格子达',3,1),('PaperYY',4,1),('笔杆网',5,1),
('万方',6,1),('PaperPass',7,1),('华宸',8,1),('paperred',9,1),
('writepass',10,1),('papered',11,1),('大雅',12,1),('朱雀',13,1);

CREATE TABLE module_platform (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    platform_id BIGINT NOT NULL COMMENT '平台ID',
    module_code VARCHAR(50) NOT NULL COMMENT '模块',
    module_name VARCHAR(50) NOT NULL COMMENT '模块名称',
    language VARCHAR(20) NOT NULL DEFAULT 'chinese' COMMENT '语言',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_plat_module_lang (platform_id, module_code, language),
    KEY idx_module (module_code),
    KEY idx_lang (language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模块平台关联表';

-- 所有平台默认关联全部模块+语言
INSERT INTO module_platform (platform_id, module_code, module_name, language, sort_order, status)
SELECT p.id, m.code, m.name, l.val, 0, 1
FROM platform p
CROSS JOIN (
    SELECT 'repeat_reduce' AS code, '降重复率' AS name
    UNION ALL SELECT 'ai_reduce', '降AI率'
    UNION ALL SELECT 'dual_reduce', '降重+降AIGC'
) m
CROSS JOIN (
    SELECT 'chinese' AS val UNION ALL SELECT 'english'
) l;

CREATE TABLE preset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(100) NOT NULL COMMENT '预设代码',
    name VARCHAR(200) NOT NULL COMMENT '预设名称',
    category VARCHAR(50) NOT NULL COMMENT '分类: 降AI/降重/双降/AI检测',
    price DECIMAL(12,4) DEFAULT NULL COMMENT '单价(元/千字符)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_code (code),
    KEY idx_category (category),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='改写预设表';

INSERT INTO preset (code, name, category, price, status, sort_order) VALUES
('aigcmove_tp_s_merge_1', '降ai内测模式-混合模式', '降AI', 0.44, 1, 1),
('aigcmove_tp_s_micro_1', '降ai内测模式-微度模式1', '降AI', 0.44, 1, 2),
('aigcmove_tp_s_micro_2', '降ai内测模式-微度模式2', '降AI', 0.44, 1, 3),
('aigcmove_tp_s_micro_3', '降ai内测模式-微度模式3', '降AI', 0.44, 1, 4),
('aigcmove_tp_s_micro_4', '降ai内测模式-微度模式4', '降AI', 0.44, 1, 5),
('aigcmove_tp_s_micro_5', '降ai内测模式-微度模式5', '降AI', 0.44, 1, 6),
('aimove_weipu_0417', '0417_zh_强力改写', '降AI', 0.44, 1, 7),
('gzd_0510', '0510-格子达个人版', '降AI', 0.44, 1, 8),
('aigcmove_tp_s_1', 'AI移除_260112_1 (降AI能力一般，改写幅度小，学术性强，非一逗到底）', '降AI', 0.44, 1, 9),
('aigcmove_tp_s_2', 'AI移除_260112_2 (降AI能力一般，改写幅度小，学术性强，非一逗到底）', '降AI', 0.44, 1, 10),
('aigcmove_tp_s_3', 'AI移除_260112_3 (降AI能力强，改写幅度中，学术性中，非一逗到底）', '降AI', 0.44, 1, 11),
('aigcmove_tp_s_4', 'AI移除_260112_4 (降AI能力强，改写幅度中，学术性中，非一逗到底）', '降AI', 0.44, 1, 12),
('aigcmove_tp_s_5', 'AI移除_260112_5 (降AI能力很强，改写幅度大，学术性中，非一逗到底）', '降AI', 0.44, 1, 13),
('turnitin_s_1', 'turnitin260430(质量高)', '降AI', 0.44, 1, 14),
('aimove_大雅句子_1', 'AI移除_12.14_对大雅，YY降AI力度强', '降AI', 0.44, 1, 15),
('jc_remove_djgzl_p_8', 'AI移除_5.09(降AI能力很强，口语化+一逗到底）', '降AI', 0.44, 1, 16),
('aimove_matrix_s_1', 'AI移除_10.24(降小说新闻等泛娱乐模型)', '降AI', 0.44, 1, 17),
('aimove_weipu_english', '降AI260430_en', '降AI', 0.44, 1, 18),
('zh_jc_1_0325', '降重模式0325-1', '降重', 0.50, 1, 19),
('zh_jc_2_0325', '降重模式0325-2', '降重', 0.50, 1, 20),
('zh_jc_3_0325', '降重模式0325-3', '降重', 0.50, 1, 21),
('zh_jc_4_0325', '降重模式0325-4', '降重', 0.50, 1, 22),
('jc_muti_language_1', '超级改写(260111_1_en_ja_ru)', '降重', 0.44, 1, 23),
('jc_muti_language_zh', '降重模式260206_zh', '降重', 0.44, 1, 24),
('aigcmove_sj_s_1', '双降260112_1', '双降', 0.44, 1, 25),
('aigcmove_sj_s_2', '双降260112_2', '双降', 0.44, 1, 26),
('aigcmove_sj_s_3', '双降260112_3', '双降', 0.44, 1, 27),
('5.09en', '双降260206_en', '双降', 0.44, 1, 28);

DROP TABLE IF EXISTS platform_preset;

CREATE TABLE platform_preset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台名称',
    module_code VARCHAR(50) NOT NULL COMMENT '模块: repeat_reduce/ai_reduce/dual_reduce',
    module_name VARCHAR(50) NOT NULL COMMENT '模块名称: 降重复率/降AI率/降重+降AIGC',
    language VARCHAR(20) NOT NULL DEFAULT 'chinese' COMMENT '语言: chinese/english',
    preset_code VARCHAR(100) NOT NULL COMMENT '预设代码',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_platform_module_lang_preset (platform, module_code, language, preset_code),
    KEY idx_platform (platform),
    KEY idx_module (module_code),
    KEY idx_language (language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台-模块-语言-预设关联表';

INSERT INTO platform_preset (platform, module_code, module_name, language, preset_code, sort_order, status) VALUES
('知网','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('知网','ai_reduce','降AI率','chinese','aigcmove_tp_s_2',2,1),
('知网','ai_reduce','降AI率','chinese','aigcmove_tp_s_3',3,1),
('知网','ai_reduce','降AI率','chinese','aigcmove_tp_s_4',4,1),
('知网','ai_reduce','降AI率','chinese','aigcmove_tp_s_5',5,1),
('知网','ai_reduce','降AI率','chinese','aigcmove_tp_s_merge_1',6,1),
('知网','ai_reduce','降AI率','chinese','aimove_weipu_0417',7,1),
('知网','ai_reduce','降AI率','chinese','turnitin_s_1',8,1),
('知网','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('知网','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('知网','repeat_reduce','降重复率','chinese','zh_jc_2_0325',2,1),
('知网','repeat_reduce','降重复率','chinese','zh_jc_3_0325',3,1),
('知网','repeat_reduce','降重复率','chinese','zh_jc_4_0325',4,1),
('知网','repeat_reduce','降重复率','chinese','jc_muti_language_zh',5,1),
('知网','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('知网','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('知网','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_2',2,1),
('知网','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_3',3,1),
('知网','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('维普','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('维普','ai_reduce','降AI率','chinese','aigcmove_tp_s_2',2,1),
('维普','ai_reduce','降AI率','chinese','aigcmove_tp_s_3',3,1),
('维普','ai_reduce','降AI率','chinese','aigcmove_tp_s_4',4,1),
('维普','ai_reduce','降AI率','chinese','aimove_weipu_0417',5,1),
('维普','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('维普','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('维普','repeat_reduce','降重复率','chinese','zh_jc_2_0325',2,1),
('维普','repeat_reduce','降重复率','chinese','jc_muti_language_zh',3,1),
('维普','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('维普','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('维普','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_2',2,1),
('维普','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('格子达','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('格子达','ai_reduce','降AI率','chinese','aigcmove_tp_s_2',2,1),
('格子达','ai_reduce','降AI率','chinese','gzd_0510',3,1),
('格子达','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('格子达','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('格子达','repeat_reduce','降重复率','chinese','zh_jc_2_0325',2,1),
('格子达','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('格子达','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('格子达','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('PaperYY','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('PaperYY','ai_reduce','降AI率','chinese','aigcmove_tp_s_2',2,1),
('PaperYY','ai_reduce','降AI率','chinese','aimove_大雅句子_1',3,1),
('PaperYY','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('PaperYY','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('PaperYY','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('PaperYY','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('PaperYY','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('笔杆网','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('笔杆网','ai_reduce','降AI率','chinese','aigcmove_tp_s_2',2,1),
('笔杆网','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('笔杆网','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('笔杆网','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('笔杆网','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('笔杆网','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('万方','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('万方','ai_reduce','降AI率','chinese','aigcmove_tp_s_2',2,1),
('万方','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('万方','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('万方','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('万方','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('万方','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('PaperPass','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('PaperPass','ai_reduce','降AI率','chinese','aigcmove_tp_s_2',2,1),
('PaperPass','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('PaperPass','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('PaperPass','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('PaperPass','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('PaperPass','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('华宸','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('华宸','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('华宸','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('华宸','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('华宸','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('华宸','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('paperred','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('paperred','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('paperred','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('paperred','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('paperred','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('paperred','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('writepass','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('writepass','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('writepass','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('writepass','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('writepass','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('writepass','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('papered','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('papered','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('papered','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('papered','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('papered','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('papered','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('大雅','ai_reduce','降AI率','chinese','aimove_大雅句子_1',1,1),
('大雅','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',2,1),
('大雅','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('大雅','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('大雅','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('大雅','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('大雅','dual_reduce','降重+降AIGC','english','5.09en',1,1),
('朱雀','ai_reduce','降AI率','chinese','aigcmove_tp_s_1',1,1),
('朱雀','ai_reduce','降AI率','chinese','aigcmove_tp_s_2',2,1),
('朱雀','ai_reduce','降AI率','english','aimove_weipu_english',1,1),
('朱雀','repeat_reduce','降重复率','chinese','zh_jc_1_0325',1,1),
('朱雀','repeat_reduce','降重复率','english','jc_muti_language_1',1,1),
('朱雀','dual_reduce','降重+降AIGC','chinese','aigcmove_sj_s_1',1,1),
('朱雀','dual_reduce','降重+降AIGC','english','5.09en',1,1);

DROP TABLE IF EXISTS announcement;

CREATE TABLE announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1发布 0草稿',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

INSERT INTO announcement (title, content, status) VALUES
('系统正式上线', '论文降重降AI率系统正式上线运行，支持13个主流查重平台的文本和文档改写。用户可通过卡密兑换或联系客服充值余额。', 1),
('价格说明', '文本改写与文档改写均按字符数计费，实际扣费 = API原始费用 × 1.4（加价倍率）。余额不足1元时无法改写，请及时充值。', 1);

INSERT INTO sys_user (username, phone, password, role, status)
VALUES ('admin', '18800000000', '$2b$10$nnmpsMrICxCPMswIt2xPzOmY4KnjlgZ81O7z3RzEWp3MRqMgiQJPW', 'ADMIN', 1);
