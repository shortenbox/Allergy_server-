
USE infant_meal_db;

-- =========================
-- 1. 알러지 기준 테이블
-- =========================
CREATE TABLE IF NOT EXISTS allergen_in (
    allergen_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    allergen_name VARCHAR(100) NOT NULL UNIQUE,
    category_name VARCHAR(50) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 2. 음식 별칭 테이블
-- =========================
CREATE TABLE IF NOT EXISTS ingredient_alias (
    alias_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alias_name VARCHAR(100) NOT NULL,
    standard_name VARCHAR(100) NOT NULL,
    category_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ingredient_alias_alias_name (alias_name)
);

-- =========================
-- 3. 9종 카테고리 테이블
-- =========================
CREATE TABLE IF NOT EXISTS allergy_category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- =========================
-- 4. 결과 저장 테이블
-- =========================
CREATE TABLE IF NOT EXISTS allergy_result (
    result_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    input_food VARCHAR(100) NOT NULL,
    category_name VARCHAR(50),
    risk_level VARCHAR(20),
    nutrition_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 5. 9종 카테고리 데이터
-- =========================
INSERT IGNORE INTO allergy_category (category_name, description) VALUES
('난류', '계란, 메추리알'),
('유제품', '우유, 치즈, 버터, 요구르트'),
('곡류', '밀, 메밀, 대두'),
('갑각류', '새우, 게'),
('견과류', '땅콩, 호두, 잣'),
('생선류', '고등어'),
('가공육', '햄, 소시지 (아황산 포함)'),
('연체류', '오징어, 조개류'),
('육류/과채류', '닭고기, 돼지고기, 소고기, 토마토, 복숭아');

-- =========================
-- 6. 알러지 기준 데이터 (전부 위험)
-- =========================
INSERT IGNORE INTO allergen_in (allergen_name, category_name, risk_level, description) VALUES
('우유', '유제품', '위험', '우유 및 유제품'),
('치즈', '유제품', '위험', '치즈'),
('계란', '난류', '위험', '계란'),
('땅콩', '견과류', '위험', '땅콩'),
('대두', '곡류', '위험', '콩류'),
('밀', '곡류', '위험', '밀가루'),
('메밀', '곡류', '위험', '메밀'),
('호두', '견과류', '위험', '호두'),
('잣', '견과류', '위험', '잣'),
('새우', '갑각류', '위험', '새우'),
('게', '갑각류', '위험', '게'),
('오징어', '연체류', '위험', '오징어'),
('고등어', '생선류', '위험', '고등어'),
('조개', '연체류', '위험', '조개류'),
('토마토', '육류/과채류', '위험', '토마토'),
('복숭아', '육류/과채류', '위험', '복숭아'),
('닭고기', '육류/과채류', '위험', '닭고기'),
('돼지고기', '육류/과채류', '위험', '돼지고기'),
('소고기', '육류/과채류', '위험', '소고기');

-- =========================
-- 7. OCR 별칭 데이터
-- =========================
INSERT IGNORE INTO ingredient_alias (alias_name, standard_name, category_name) VALUES
('우유분말', '우유', '유제품'),
('유청단백', '우유', '유제품'),
('치즈', '우유', '유제품'),
('버터', '우유', '유제품'),
('난백', '계란', '난류'),
('전란액', '계란', '난류'),
('달걀', '계란', '난류'),
('땅콩버터', '땅콩', '견과류'),
('대두단백', '대두', '곡류'),
('콩', '대두', '곡류'),
('간장', '대두', '곡류'),
('밀가루', '밀', '곡류'),
('소맥분', '밀', '곡류'),
('새우분말', '새우', '갑각류'),
('꽃게추출물', '게', '갑각류'),
('새우젓', '새우', '갑각류'),
('바지락', '조개', '연체류'),
('홍합', '조개', '연체류'),
('오징어', '오징어', '연체류'),
('주꾸미', '오징어', '연체류'),
('토마토농축액', '토마토', '육류/과채류'),
('복숭아농축액', '복숭아', '육류/과채류');

-- =========================
-- 8. 테스트
-- =========================
SELECT * FROM allergen_in;
SELECT * FROM ingredient_alias;
SELECT * FROM allergy_category;
SELECT * FROM allergy_result;

SELECT standard_name, category_name
FROM ingredient_alias
WHERE alias_name = '우유분말';

SELECT * FROM ingredient_alias WHERE alias_name LIKE '%달걀%';




