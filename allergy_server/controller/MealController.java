package com.example.allergy_server.controller;

import com.example.allergy_server.service.MealService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * =========================================
 * MealController
 * =========================================
 * [역할]
 * - 클라이언트 API 요청 진입점
 * - MealService로 요청 전달
 * - REST API 라우팅 담당 (/api/meal)
 *
 * [성능]
 * - 비즈니스 로직 없음 (Thin Controller)
 * - 매우 가벼움 → 고부하 영향 거의 없음
 *
 * [특징]
 * - 요청/응답만 처리
 * - 서비스 계층 분리 구조 유지
 * =========================================
 */

@RestController
@RequestMapping("/api/meal")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping("/analyze")
    public Map<String, Object> analyze(
            @RequestBody Map<String, String> request
    ) {
        return mealService.analyzeMeal(request);
    }
}
