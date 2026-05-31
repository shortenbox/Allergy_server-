-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS infant_meal_db;
USE infant_meal_db;

-- user table 생성
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SHOW TABLES;

INSERT IGNORE INTO users (login_id, password, name)
VALUES ('user01', '1234', '홍길동');

SELECT * FROM users;

SELECT user, host FROM mysql.user;

-- meal_images 테이블은 Mealmage.sql에서 생성/관리한다.
