CREATE DATABASE IF NOT EXISTS mall_product DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_product;

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT,
    level TINYINT NOT NULL,
    sort_order INT DEFAULT 0,
    icon_url VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    subtitle VARCHAR(500),
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    stock INT NOT NULL DEFAULT 0,
    sales_count INT DEFAULT 0,
    images JSON NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    reject_reason VARCHAR(500),
    version INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    KEY idx_category_status (category_id, status),
    KEY idx_merchant_status (merchant_id, status)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
