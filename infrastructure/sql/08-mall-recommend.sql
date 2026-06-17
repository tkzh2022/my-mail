CREATE DATABASE IF NOT EXISTS mall_recommend DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_recommend;

CREATE TABLE recommendation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    score DOUBLE NOT NULL,
    algorithm_version VARCHAR(20) NOT NULL,
    reason VARCHAR(50),
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_product_algorithm (user_id, product_id, algorithm_version)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
