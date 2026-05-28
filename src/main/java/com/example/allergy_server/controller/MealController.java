package com.example.allergy_server.controller;

import com.example.allergy_server.service.MealService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * =========================================
 * MealController
 * =========================================
 * [역할]
 * - 텍스트 기반 식단 분석 API 처리
 * - 사용자가 입력한 음식 데이터를 분석 서비스로 전달
 * - REST API 요청 및 응답 관리
 *
 * [기능]
 * - /api/meal/analyze
 * - JSON 식단 입력 처리
 * - 음식별 영양 정보 분석
 * - 알러지 위험 결과 반환
 *
 * [특징]
 * - Thin Controller 구조
 * - 비즈니스 로직 없음
 * - Service 계층 호출 전담
 *
 * [처리 흐름]
 * 요청(JSON)
 * → MealService.analyzeMeal()
 * → 음식 분석
 * → 결과 JSON 반환
 * =========================================
 */

@RestController
@RequestMapping("/api/meal")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    // =========================
    // 1. 텍스트 기반 식단 분석
    // =========================
    @PostMapping("/analyze/text")
    public Map<String, Object> analyze(
            @RequestBody Map<String, String> request
    ) {
        return mealService.analyzeMeal(request);
    }
}
