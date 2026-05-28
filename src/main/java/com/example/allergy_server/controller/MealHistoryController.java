package com.example.allergy_server.controller;

import com.example.allergy_server.service.MealHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 분석 기록 조회 API.
 * 이미지 분석 API에서 저장된 최근 기록과 상세 기록을 반환한다.
 */
@RestController
@RequestMapping("/api/meal/history")
public class MealHistoryController {

    private final MealHistoryService mealHistoryService;

    public MealHistoryController(MealHistoryService mealHistoryService) {
        this.mealHistoryService = mealHistoryService;
    }

    @GetMapping
    public List<Map<String, Object>> findRecent() {
        // 최근 저장된 분석 기록 목록 조회
        return mealHistoryService.findRecent();
    }

    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable Long id) {
        // 특정 분석 기록 상세 조회
        return mealHistoryService.findById(id);
    }
}
