package com.example.allergy_server.service;

import com.example.allergy_server.external_ocr.OcrPicture;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * =========================================
 * MealService
 * =========================================
 * [역할]
 * - 식단 분석 핵심 파이프라인
 *   OCR → 음식 추출 → alias → 영양 API → 재료 → 알러지 체크
 *
 * [성능]
 * - 가장 무거운 서비스 계층
 * - 외부 API + DB + OCR 호출 포함
 * - 병목 발생 가능 (네트워크 + DB + OCR)
 *
 * [특징]
 * - 시스템 “핵심 엔진”
 * - 모든 데이터 흐름 통합 담당
 *
 * [성능 개선 포인트]
 * - alias 캐싱 필요
 * - Food API 응답 캐싱 필요
 * - OCR 비동기 처리 가능
 * =========================================
 */

@Service
public class MealService {

    private final OcrPicture ocrPicture;
    private final FoodApiService foodApiService;
    private final CheckAllergyService checkAllergyService;
    private final IngredientAliasService ingredientAliasService; // 🔥 추가

    // =========================
    // 생성자
    // =========================
    public MealService(OcrPicture ocrPicture,
                       FoodApiService foodApiService,
                       CheckAllergyService checkAllergyService,
                       IngredientAliasService ingredientAliasService) {

        this.ocrPicture = ocrPicture;
        this.foodApiService = foodApiService;
        this.checkAllergyService = checkAllergyService;
        this.ingredientAliasService = ingredientAliasService;
    }

    // =========================
    // 1. 텍스트 기반 분석 API
    // =========================
    public Map<String, Object> analyzeMeal(Map<String, String> meals) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("breakfast", analyzeOne(meals.get("breakfast")));
        result.put("lunch", analyzeOne(meals.get("lunch")));
        result.put("dinner", analyzeOne(meals.get("dinner")));
        result.put("snack", analyzeOne(meals.get("snack")));

        return result;
    }

    // =========================
    // 2. 이미지 기반 분석 API
    // =========================
    public Map<String, Object> processImage(MultipartFile image) {

        Map<String, Object> result = new LinkedHashMap<>();

        try {
            String ocrText = ocrPicture.extractText(image);

            List<String> foods = extractFoods(ocrText);

            Map<String, Object> analysis = new LinkedHashMap<>();

            for (String food : foods) {
                analysis.put(food, analyzeOne(food));
            }

            result.put("ocrText", ocrText);
            result.put("foods", foods);
            result.put("analysis", analysis);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "이미지 분석 실패");
        }

        return result;
    }

    // =========================
    // 3. 핵심 분석 로직
    // =========================
    private Map<String, Object> analyzeOne(String food) {

        Map<String, Object> item = new LinkedHashMap<>();

        if (food == null || food.isBlank()) {
            item.put("foodName", "");
            item.put("risk", "입력 없음");
            return item;
        }

        // 🔥 1. 전처리 (핵심)
        food = food.trim();
        food = food.replaceAll("\\(.*?\\)", "");

        System.out.println("RAW FOOD = " + food);

        // 🔥 2. alias 변환
        String standardFood = ingredientAliasService.convert(food);

        System.out.println("STANDARD FOOD = " + standardFood);

        item.put("foodName", standardFood);

        // 3. 영양 정보
        item.putAll(foodApiService.analyze(standardFood));

        // 4. 재료
        List<String> ingredients = extractIngredientsFromFood(standardFood);
        item.put("ingredients", ingredients);

        // 5. 알러지 체크
        String risk = checkAllergyService.check(standardFood);
        item.put("risk", risk);

        return item;
    }

    // =========================
    // 4. 재료 추출
    // =========================
    private List<String> extractIngredientsFromFood(String food) {

        try {
            String raw = foodApiService.getRecipeParts(food);

            if (raw == null || raw.isBlank()) {
                return List.of();
            }

            return Arrays.stream(raw.split("[,·\\n/]"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // =========================
    // 5. OCR → 음식 추출
    // =========================
    private List<String> extractFoods(String text) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        String[] tokens = text.split("\\s+");

        List<String> foods = new ArrayList<>();

        for (String t : tokens) {
            String word = t.replaceAll("[^가-힣a-zA-Z]", "");

            if (word.length() >= 2) {
                foods.add(word);
            }
        }

        return new ArrayList<>(new LinkedHashSet<>(foods));
    }
}