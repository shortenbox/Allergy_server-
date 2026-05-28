package com.example.allergy_server.service;

import com.example.allergy_server.entity.WeeklyMealPlan;
import com.example.allergy_server.repository.WeeklyMealPlanRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 날짜별 식단표 저장 및 조회 서비스.
 * 저장 시 MealService로 식사별 분석 결과를 생성하고 JSON 문자열로 보관한다.
 */
@Service
public class WeeklyMealPlanService {

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final MealService mealService;
    private final ObjectMapper objectMapper;

    public WeeklyMealPlanService(WeeklyMealPlanRepository weeklyMealPlanRepository,
                                 MealService mealService,
                                 ObjectMapper objectMapper) {
        this.weeklyMealPlanRepository = weeklyMealPlanRepository;
        this.mealService = mealService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> save(Map<String, String> request) {

        LocalDate planDate = parsePlanDate(request.get("date"));
        Map<String, String> meals = extractMeals(request);
        Map<String, Object> analysis = mealService.analyzeMeal(meals);

        try {
            WeeklyMealPlan weeklyMealPlan = weeklyMealPlanRepository
                    .findByPlanDate(planDate)
                    .orElseGet(WeeklyMealPlan::new);

            weeklyMealPlan.setPlanDate(planDate);
            weeklyMealPlan.setBreakfast(clean(request.get("breakfast")));
            weeklyMealPlan.setLunch(clean(request.get("lunch")));
            weeklyMealPlan.setDinner(clean(request.get("dinner")));
            weeklyMealPlan.setSnack(clean(request.get("snack")));
            weeklyMealPlan.setAnalysisJson(objectMapper.writeValueAsString(analysis));

            return toResponse(weeklyMealPlanRepository.save(weeklyMealPlan));

        } catch (Exception e) {
            e.printStackTrace();

            return Map.of(
                    "status", "ERROR",
                    "message", "주간 식단표 저장 중 오류가 발생했습니다."
            );
        }
    }

    public List<Map<String, Object>> findBetween(String startDate, String endDate) {
        return weeklyMealPlanRepository
                .findByPlanDateBetweenOrderByPlanDateAsc(
                        LocalDate.parse(startDate),
                        LocalDate.parse(endDate)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Map<String, Object> findByDate(String date) {
        return weeklyMealPlanRepository.findByPlanDate(LocalDate.parse(date))
                .map(this::toResponse)
                .orElseGet(() -> Map.of(
                        "status", "NOT_FOUND",
                        "message", "해당 날짜의 식단표를 찾을 수 없습니다."
                ));
    }

    public Map<String, Object> findById(Long id) {
        return weeklyMealPlanRepository.findById(id)
                .map(this::toResponse)
                .orElseGet(() -> Map.of(
                        "status", "NOT_FOUND",
                        "message", "식단표 기록을 찾을 수 없습니다."
                ));
    }

    private LocalDate parsePlanDate(String date) {

        if (date == null || date.isBlank()) {
            return LocalDate.now();
        }

        return LocalDate.parse(date);
    }

    private Map<String, String> extractMeals(Map<String, String> request) {
        Map<String, String> meals = new LinkedHashMap<>();

        putIfPresent(meals, "breakfast", request.get("breakfast"));
        putIfPresent(meals, "lunch", request.get("lunch"));
        putIfPresent(meals, "dinner", request.get("dinner"));
        putIfPresent(meals, "snack", request.get("snack"));

        return meals;
    }

    private void putIfPresent(Map<String, String> meals, String key, String value) {

        if (value != null && !value.isBlank()) {
            meals.put(key, value.trim());
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private Map<String, Object> toResponse(WeeklyMealPlan weeklyMealPlan) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", weeklyMealPlan.getId());
        response.put("date", weeklyMealPlan.getPlanDate());
        response.put("breakfast", weeklyMealPlan.getBreakfast());
        response.put("lunch", weeklyMealPlan.getLunch());
        response.put("dinner", weeklyMealPlan.getDinner());
        response.put("snack", weeklyMealPlan.getSnack());
        response.put("analysis", parseAnalysisJson(weeklyMealPlan.getAnalysisJson()));
        response.put("createdAt", weeklyMealPlan.getCreatedAt());
        response.put("updatedAt", weeklyMealPlan.getUpdatedAt());

        return response;
    }

    private Object parseAnalysisJson(String json) {

        if (json == null || json.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {
                    }
            );

        } catch (Exception e) {
            return json;
        }
    }
}
