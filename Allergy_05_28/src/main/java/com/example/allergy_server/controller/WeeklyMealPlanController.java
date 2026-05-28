package com.example.allergy_server.controller;

import com.example.allergy_server.service.WeeklyMealPlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 주간 식단표 저장/조회 API.
 * 날짜별 아침/점심/저녁/간식을 저장하고 다시 식단표 형태로 조회한다.
 */
@RestController
@RequestMapping("/api/meal/weekly")
public class WeeklyMealPlanController {

    private final WeeklyMealPlanService weeklyMealPlanService;

    public WeeklyMealPlanController(WeeklyMealPlanService weeklyMealPlanService) {
        this.weeklyMealPlanService = weeklyMealPlanService;
    }

    @PostMapping
    public Map<String, Object> save(@RequestBody Map<String, String> request) {
        return weeklyMealPlanService.save(request);
    }

    @GetMapping
    public List<Map<String, Object>> findBetween(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return weeklyMealPlanService.findBetween(startDate, endDate);
    }

    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable Long id) {
        return weeklyMealPlanService.findById(id);
    }

    @GetMapping("/date/{date}")
    public Map<String, Object> findByDate(@PathVariable String date) {
        return weeklyMealPlanService.findByDate(date);
    }
}
