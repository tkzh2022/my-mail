CREATE DATABASE IF NOT EXISTS mall_notification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_notification;

CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    channel VARCHAR(10) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read TINYINT DEFAULT 0,
    sent_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_user_read_created (user_id, is_read, created_at)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
