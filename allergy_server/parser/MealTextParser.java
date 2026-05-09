package com.example.allergy_server.parser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ==========================================
 * MealTextParser
 * ==========================================
 * OCR 또는 입력 텍스트 전처리 서비스
 *
 * [역할]
 * - OCR 텍스트에서 음식 이름 추출
 * - 특수문자 제거
 * - 중복 제거
 *
 * [특징]
 * - 자연어 → 구조화 데이터 변환
 * ==========================================
 */

public class MealTextParser {

    public static Map<String, String> parse(String text) {

        Map<String, String> result = new LinkedHashMap<>();

        if (text == null || text.isBlank()) {
            return result;
        }

        result.put("breakfast", extract(text, "아침"));
        result.put("lunch", extract(text, "점심"));
        result.put("dinner", extract(text, "저녁"));
        result.put("snack", extract(text, "간식"));

        return result;
    }

    private static String extract(String text, String keyword) {

        for (String token : text.split("\\s+")) {

            if (token.contains(keyword)) {
                return token
                        .replace(keyword, "")
                        .replace(":", "")
                        .trim();
            }
        }

        return "";
    }
}
