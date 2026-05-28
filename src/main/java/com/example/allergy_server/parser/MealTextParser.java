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

        String[] lines = text.split("\\n");
        String currentKey = null;

        for (String line : lines) {

            line = line.trim();
            if (line.isBlank()) continue;

            String key = getMealKey(line);

            if (key != null) {
                currentKey = key;

                String foodText = removeMealLabel(line);

                if (!foodText.isBlank()) {
                    append(result, currentKey, foodText);
                }

                continue;
            }

            if (currentKey != null) {
                append(result, currentKey, line);
            }
        }

        return result;
    }

    private static String getMealKey(String line) {

        String normalized = line.replaceAll("\\s+", "");

        if (normalized.startsWith("아침") || normalized.startsWith("조식")) {
            return "breakfast";
        }

        if (normalized.startsWith("점심") || normalized.startsWith("중식")) {
            return "lunch";
        }

        if (normalized.startsWith("저녁") || normalized.startsWith("석식")) {
            return "dinner";
        }

        if (normalized.startsWith("간식")) {
            return "snack";
        }

        return null;
    }

    private static String removeMealLabel(String line) {
        return line
                .replaceFirst("^\\s*(아침|조식|점심|중식|저녁|석식|간식)\\s*[:：\\-]?", "")
                .trim();
    }

    private static void append(Map<String, String> result, String key, String value) {
        String current = result.get(key);

        if (current == null || current.isBlank()) {
            result.put(key, value.trim());
            return;
        }

        result.put(key, current + " " + value.trim());
    }
}
