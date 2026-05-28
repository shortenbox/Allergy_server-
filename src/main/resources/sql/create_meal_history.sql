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
