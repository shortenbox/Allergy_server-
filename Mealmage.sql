USE infant_meal_db;
SHOW TABLES;

CREATE TABLE IF NOT EXISTS meal_images (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_path TEXT NOT NULL,
    analysis_result LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
SELECT * FROM meal_images;

SHOW DATABASES;

-- 이미지 분석 기록 저장 테이블
-- image_path: 서버에 저장된 업로드 이미지 경로
-- ocr_text: OCR 원문 텍스트
-- analysis_json: 클라이언트에 반환한 최종 분석 JSON 문자열
CREATE TABLE IF NOT EXISTS meal_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_path TEXT,
    ocr_text LONGTEXT,
    analysis_json LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SELECT * FROM meal_history ORDER BY created_at DESC;

-- 날짜별 주간 식단표 저장 테이블
-- plan_date: 식단 날짜
-- breakfast/lunch/dinner/snack: 식사별 음식명
-- analysis_json: 식사별 분석 결과 JSON 문자열
CREATE TABLE IF NOT EXISTS weekly_meal_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_date DATE NOT NULL UNIQUE,
    breakfast TEXT,
    lunch TEXT,
    dinner TEXT,
    snack TEXT,
    analysis_json LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

SELECT * FROM weekly_meal_plan ORDER BY plan_date DESC;
