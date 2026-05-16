package com.example.allergy_server.service;

import com.example.allergy_server.external_ocr.OcrPicture;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class MealService {

    private final OcrPicture ocrPicture;
    private final FoodApiService foodApiService;
    private final CheckAllergyService checkAllergyService;
    private final IngredientAliasService ingredientAliasService;

    // =========================
    // 🔥 병렬 처리 스레드풀
    // =========================
    private final ExecutorService executor =
            Executors.newFixedThreadPool(8);

    public MealService(
            OcrPicture ocrPicture,
            FoodApiService foodApiService,
            CheckAllergyService checkAllergyService,
            IngredientAliasService ingredientAliasService
    ) {
        this.ocrPicture = ocrPicture;
        this.foodApiService = foodApiService;
        this.checkAllergyService = checkAllergyService;
        this.ingredientAliasService = ingredientAliasService;
    }

    // =========================
    // 텍스트 분석 API
    // =========================
    public Map<String, Object> analyzeMeal(Map<String, String> meals) {

        Map<String, Object> result = new LinkedHashMap<>();

        List<String> keys = List.of("breakfast", "lunch", "dinner", "snack");

        for (String key : keys) {

            String value = meals.get(key);

            if (value == null || value.isBlank()) continue;

            result.put(key, analyzeOne(value));
        }

        return result;
    }

    // =========================
    // 이미지 분석 API (🔥 병렬 적용)
    // =========================
    public Map<String, Object> processImage(MultipartFile image) {

        Map<String, Object> result = new LinkedHashMap<>();

        try {

            String ocrText = extractMealOcr(image);
            List<String> foods = extractFoods(ocrText);

            Map<String, Object> analysis =
                    foods.stream()
                            .map(food ->
                                    CompletableFuture.supplyAsync(
                                            () -> Map.entry(food, analyzeOne(food)),
                                            executor
                                    )
                            )
                            .map(CompletableFuture::join)
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));

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
    // OCR 전략
    // =========================
    private String extractMealOcr(MultipartFile image) {

        String clovaText = ocrPicture.extractMealText(image);

        if (isMealTable(clovaText)) {
            return clovaText;
        }

        return ocrPicture.extractGoogleText(image);
    }

    private boolean isMealTable(String text) {

        if (text == null || text.isBlank()) return false;

        return text.contains("조식")
                || text.contains("중식")
                || text.contains("석식")
                || text.contains("월요일")
                || text.contains("식단")
                || text.contains("메뉴");
    }

    // =========================
    // 핵심 분석
    // =========================
    private Map<String, Object> analyzeOne(String food) {

        Map<String, Object> item = new LinkedHashMap<>();

        if (food == null || food.isBlank()) {
            item.put("foodName", "");
            item.put("risk", "입력 없음");
            return item;
        }

        food = food.trim().replaceAll("\\(.*?\\)", "");

        String standardFood =
                ingredientAliasService.convert(food);

        item.put("foodName", standardFood);

        item.putAll(
                foodApiService.analyze(standardFood)
        );

        List<String> ingredients =
                extractIngredientsFromFood(standardFood);

        item.put("ingredients", ingredients);

        String risk =
                checkAllergyService.check(standardFood, ingredients);

        item.put("risk", risk);

        return item;
    }

    private List<String> extractIngredientsFromFood(String food) {

        try {

            String raw =
                    foodApiService.getRecipeParts(food);

            if (raw == null || raw.isBlank()) {
                return List.of();
            }

            return Arrays.stream(raw.split("[,·\\n/]"))
                    .map(s -> s.replaceAll("재료", ""))
                    .map(s -> s.replaceAll("\\(.*?\\)", ""))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .toList();

        } catch (Exception e) {
            return List.of();
        }
    }

    // =========================
    // OCR → 음식 추출
    // =========================
    private List<String> extractFoods(String text) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        String[] lines = text.split("\\n|·");

        List<String> foods = new ArrayList<>();

        for (String line : lines) {

            line = normalizeLine(line);
            if (line.isBlank()) continue;

            if (isMealLabel(line)) continue;

            if (line.contains("식단표")) continue;
            if (line.contains("하루")) continue;
            if (line.contains("균형")) continue;
            if (line.contains("잡힌")) continue;
            if (line.contains("보내세요")) continue;

            if (isNoiseLine(line)) continue;

            if (!foods.contains(line)) {
                foods.add(line);
            }
        }

        return foods;
    }

    // =========================
    // 전처리
    // =========================
    private String normalizeLine(String line) {

        if (line == null) return "";

        line = line.trim();

        line = line.replaceAll("[0-9()/.,~\\-]", "");
        line = line.replaceAll("[^가-힣a-zA-Z\\s]", "");

        line = line.replaceAll("\\d{1,3}(,\\d{3})?\\s*원", "");
        line = line.replaceAll("\\d+\\s?(g|ml|kg|l|인분|개|조각|컵|개입)", "");
        line = line.replaceAll("반\\s*개", "");

        return line.trim();
    }

    private boolean isMealLabel(String line) {

        if (line == null) return true;

        line = line.replaceAll("\\s+", "");

        return line.equals("아침")
                || line.equals("점심")
                || line.equals("저녁")
                || line.equals("간식")
                || line.equals("조식")
                || line.equals("중식")
                || line.equals("석식");
    }

    private boolean isNoiseLine(String line) {

        if (line == null) return true;

        return line.length() < 2
                || line.contains("균형")
                || line.contains("잡힌")
                || line.contains("건강한")
                || line.contains("하루")
                || line.contains("보내세요")
                || line.contains("식단")
                || line.contains("메뉴")
                || line.contains("구분")
                || line.equals("플레인");
    }

    // =========================
    // 호환용
    // =========================
    public String checkAllergyCompat(String food) {
        return checkAllergyService.check(food, Collections.emptyList());
    }
}