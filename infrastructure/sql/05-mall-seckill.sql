CREATE DATABASE IF NOT EXISTS mall_seckill DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_seckill;

CREATE TABLE seckill_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    seckill_price DECIMAL(10, 2) NOT NULL,
    total_stock INT NOT NULL,
    available_stock INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    per_user_limit INT DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_product_id (product_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
