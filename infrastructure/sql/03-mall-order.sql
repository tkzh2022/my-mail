CREATE DATABASE IF NOT EXISTS mall_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_order;

CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    pay_amount DECIMAL(10, 2) NOT NULL,
    freight_amount DECIMAL(10, 2) DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0,
    payment_method VARCHAR(20),
    payment_time DATETIME,
    shipping_time DATETIME,
    delivery_time DATETIME,
    complete_time DATETIME,
    tracking_company VARCHAR(50),
    tracking_number VARCHAR(50),
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(300) NOT NULL,
    remark VARCHAR(500),
    version INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_status (user_id, status),
    KEY idx_created_at (created_at)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_image VARCHAR(500) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    merchant_id BIGINT NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_no VARCHAR(64) NOT NULL,
    trade_no VARCHAR(64),
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    callback_content TEXT,
    paid_at DATETIME,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_order_id (order_id),
    UNIQUE KEY uk_payment_no (payment_no)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refund (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refund_no VARCHAR(32) NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    evidence_images JSON,
    status TINYINT NOT NULL DEFAULT 0,
    merchant_reply VARCHAR(500),
    admin_decision VARCHAR(500),
    admin_id BIGINT,
    completed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_refund_no (refund_no)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
