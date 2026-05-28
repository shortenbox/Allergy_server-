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
