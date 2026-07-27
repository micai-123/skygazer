-- V1__Init_Schema.sql
-- 智观天象数据库初始化脚本

CREATE DATABASE IF NOT EXISTS skygazer_weather DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE skygazer_weather;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    nickname VARCHAR(100),
    avatar VARCHAR(200),
    default_location VARCHAR(100),
    user_profile JSON,
    preferred_theme VARCHAR(20) DEFAULT 'auto',
    notification_enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 天气数据表
CREATE TABLE IF NOT EXISTS weather_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    location VARCHAR(100) NOT NULL,
    temperature DECIMAL(5,2),
    feels_like DECIMAL(5,2),
    humidity DECIMAL(5,2),
    wind_speed DECIMAL(5,2),
    wind_direction VARCHAR(20),
    wind_scale VARCHAR(10),
    weather_condition VARCHAR(50),
    weather_description VARCHAR(200),
    air_quality_index INT,
    air_quality_level VARCHAR(20),
    pm25 INT,
    pm10 INT,
    uv_index INT,
    visibility DECIMAL(5,2),
    pressure INT,
    precipitation DECIMAL(5,2),
    record_time DATETIME,
    data_source VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_location_time (location, record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 交互日志表
CREATE TABLE IF NOT EXISTS interaction_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    question TEXT,
    image_hash VARCHAR(64),
    answer TEXT,
    interaction_type VARCHAR(50),
    model_used VARCHAR(100),
    response_time_ms BIGINT,
    user_feedback INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_interaction_type (interaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 向量知识库表
CREATE TABLE IF NOT EXISTS vector_knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    embedding TEXT,
    category VARCHAR(50),
    title VARCHAR(100),
    metadata TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入示例知识数据
INSERT INTO vector_knowledge (content, category, title) VALUES
('积雨云是一种巨大的云层，通常预示着雷暴、暴雨等恶劣天气。如果看到积雨云，建议尽快寻找避雨场所。', 'cloud_type', '积雨云识别'),
('层云是一种低矮的灰色云层，通常带来小雨或毛毛雨。层云天气时，能见度较低，出行需注意安全。', 'cloud_type', '层云识别'),
('卷云是一种高空的白色丝状云，通常预示着天气变化。如果卷云逐渐增多，可能意味着天气将转坏。', 'cloud_type', '卷云识别'),
('哮喘患者应避免在花粉浓度高、空气质量差的天气进行户外活动。建议选择雨后或清晨进行锻炼。', 'health_advice', '哮喘患者运动建议'),
('老年人夏季出行应注意防暑降温，避免在中午时分进行户外活动。建议携带遮阳伞和充足的水。', 'health_advice', '老年人出行建议');
