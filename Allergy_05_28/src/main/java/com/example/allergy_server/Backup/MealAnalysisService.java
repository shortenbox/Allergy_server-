package com.example.allergy_server.Backup;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * =========================================
 * MealAnalysisService (Backup)
 * =========================================
 * [역할]
 * - 단순 키워드 기반 위험 분석 (rule-based)
 *
 * [성능]
 * - 매우 빠름 (DB/외부 API 없음)
 * - if 조건 기반 처리 → O(1) 수준
 *
 * [특징]
 * - 초기 테스트용 로직
 * - 현재 MealService로 기능 대부분 대체됨
 *
 * [상태]
 * - 보조/백업 용도로 유지
 * =========================================
 */

@Service
public class MealAnalysisService {

    public Map<String, Object> analyzeFoods(List<String> foods) {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        for (String food : foods) {

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("foodName", food);
            item.put("risk", analyzeRisk(food));

            list.add(item);
        }

        result.put("foods", list);
        return result;
    }

    private String analyzeRisk(String food) {

        if (food.contains("김치")) return "나트륨 주의";
        if (food.contains("우유")) return "알러지 위험";
        if (food.contains("튀김")) return "지방 주의";

        return "보통";
    }
}