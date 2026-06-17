CREATE DATABASE IF NOT EXISTS mall_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_admin;

CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    permissions JSON,
    status TINYINT NOT NULL DEFAULT 1,
    last_login_at DATETIME,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_username (username)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    created_at DATETIME NOT NULL,
    KEY idx_target (target_type, target_id),
    KEY idx_admin (admin_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO admin (username, password_hash, real_name, role, permissions, status, created_at)
VALUES (
    'admin',
    '$2a$10$77dgNuTEbgLIwfqZ2k.M8ePpj23ydCKcO9aNva53.u29gWSRdWIuG',
    'Super Admin',
    'super_admin',
    JSON_ARRAY(),
    1,
    NOW()
);
