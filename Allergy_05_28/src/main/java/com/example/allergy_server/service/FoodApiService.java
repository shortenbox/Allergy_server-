package com.example.allergy_server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FoodApiService {

    private final String apiKey = "fdca8503ce144d0088e5";

    private final String baseUrl =
            "http://openapi.foodsafetykorea.go.kr/api/";

    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================
    // 🔥 캐시 추가 (핵심)
    // =========================

    private final Map<String, Map<String, Object>> analyzeCache =
            new ConcurrentHashMap<>();

    private final Map<String, String> recipeCache =
            new ConcurrentHashMap<>();

    private final Map<String, List<String>> recipeStepsCache =
            new ConcurrentHashMap<>();

    private final Map<String, String> recipeTipCache =
            new ConcurrentHashMap<>();

    private final Map<String, JsonNode> recipeRowCache =
            new ConcurrentHashMap<>();

    private final Map<String, String> allergyCache =
            new ConcurrentHashMap<>();

    public FoodApiService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // =========================
    // 1. 영양 정보 + 캐싱 적용
    // =========================
    public Map<String, Object> analyze(String foodName) {

        if (foodName == null || foodName.isBlank()) {
            return Map.of(
                    "foodName", "",
                    "risk", "입력 없음"
            );
        }

        foodName = foodName.trim();

        // 🔥 캐시 히트
        if (analyzeCache.containsKey(foodName)) {
            return analyzeCache.get(foodName);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("foodName", foodName);

        try {
            JsonNode recipe = getRecipeRow(foodName);

            if (recipe == null) {
                String risk = checkAllergy(foodName);

                result.put("risk", risk);

                analyzeCache.put(foodName, result);
                return result;
            }

            result.put("carbohydrate", recipe.path("INFO_CAR").asText(""));
            result.put("protein", recipe.path("INFO_PRO").asText(""));
            result.put("fat", recipe.path("INFO_FAT").asText(""));
            result.put("sodium", recipe.path("INFO_NA").asText(""));

            result.put("risk", checkAllergy(foodName));

        } catch (Exception e) {
            result.put("risk", "안전");
        }

        analyzeCache.put(foodName, result);
        return result;
    }

    // =========================
    // 2. 레시피 재료 + 캐싱
    // =========================
    public String getRecipeParts(String foodName) {

        if (foodName == null || foodName.isBlank()) {
            return "";
        }

        // 🔥 캐시 히트
        if (recipeCache.containsKey(foodName)) {
            return recipeCache.get(foodName);
        }

        try {
            JsonNode recipe = getRecipeRow(foodName);

            if (recipe == null) {
                recipeCache.put(foodName, "");
                return "";
            }

            String parts =
                    recipe.path("RCP_PARTS_DTLS").asText("");

            recipeCache.put(foodName, parts);

            return parts;

        } catch (Exception e) {
            recipeCache.put(foodName, "");
            return "";
        }
    }

    // =========================
    // 3. 알러지 체크 + 캐싱
    // =========================
    public String checkAllergy(String foodName) {

        if (foodName == null || foodName.isBlank()) {
            return "안전";
        }

        // 🔥 캐시 히트
        if (allergyCache.containsKey(foodName)) {
            return allergyCache.get(foodName);
        }

        try (Connection conn = dataSource.getConnection()) {

            String standard = foodName;

            String aliasSql =
                    "SELECT standard_name FROM ingredient_alias WHERE alias_name = ?";

            try (PreparedStatement stmt = conn.prepareStatement(aliasSql)) {
                stmt.setString(1, foodName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        standard = rs.getString("standard_name");
                    }
                }
            }

            String sql =
                    "SELECT 1 FROM allergen_in WHERE allergen_name = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, standard);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        allergyCache.put(foodName, "위험");
                        return "위험";
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        allergyCache.put(foodName, "안전");
        return "안전";
    }

    // =========================
    // HTTP 요청
    // =========================
    private String request(String urlStr) throws Exception {

        URL url = new URL(urlStr);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)
        );

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    // =========================
    // JSON extract
    // =========================
    private String extract(String json, String key) {

        try {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern);

            if (start == -1) return null;

            start += pattern.length();
            int end = json.indexOf("\"", start);

            return json.substring(start, end);

        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getRecipeSteps(String foodName) {

        if (foodName == null || foodName.isBlank()) {
            return List.of();
        }

        if (recipeStepsCache.containsKey(foodName)) {
            return recipeStepsCache.get(foodName);
        }

        List<String> steps = new ArrayList<>();

        try {
            JsonNode recipe = getRecipeRow(foodName);

            if (recipe == null) {
                recipeStepsCache.put(foodName, steps);
                return steps;
            }

            for (int i = 1; i <= 20; i++) {
                String key = String.format("MANUAL%02d", i);
                String step = recipe.path(key).asText("");

                if (step != null && !step.isBlank()) {
                    steps.add(step.trim());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        recipeStepsCache.put(foodName, steps);
        return steps;
    }

    public String getRecipeTip(String foodName) {

        if (foodName == null || foodName.isBlank()) {
            return "";
        }

        if (recipeTipCache.containsKey(foodName)) {
            return recipeTipCache.get(foodName);
        }

        try {
            JsonNode recipe = getRecipeRow(foodName);

            if (recipe == null) {
                recipeTipCache.put(foodName, "");
                return "";
            }

            String tip = recipe.path("RCP_NA_TIP").asText("");
            recipeTipCache.put(foodName, tip);
            return tip;

        } catch (Exception e) {
            e.printStackTrace();
            recipeTipCache.put(foodName, "");
            return "";
        }
    }

    private JsonNode getRecipeRow(String foodName) throws Exception {

        if (foodName == null || foodName.isBlank()) {
            return null;
        }

        String normalizedFoodName = foodName.trim();

        if (recipeRowCache.containsKey(normalizedFoodName)) {
            JsonNode cached = recipeRowCache.get(normalizedFoodName);
            return cached.isMissingNode() ? null : cached;
        }

        for (String keyword : getSearchKeywords(normalizedFoodName)) {
            JsonNode recipe = requestRecipeRow(keyword);

            if (recipe != null) {
                recipeRowCache.put(normalizedFoodName, recipe);
                return recipe;
            }
        }

        recipeRowCache.put(normalizedFoodName, objectMapper.missingNode());
        return null;
    }

    private JsonNode requestRecipeRow(String keyword) throws Exception {

        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        String urlStr =
                baseUrl
                        + apiKey
                        + "/COOKRCP01/json/1/5/RCP_NM="
                        + encoded;

        String response = request(urlStr);
        JsonNode root = objectMapper.readTree(response);
        JsonNode row = root.path("COOKRCP01").path("row");

        if (!row.isArray() || row.size() == 0) {
            return null;
        }

        return row.get(0);
    }

    private List<String> getSearchKeywords(String foodName) {

        List<String> keywords = new ArrayList<>();
        keywords.add(foodName);

        if (foodName.contains("오트밀")) {
            keywords.add("오트밀죽");
            keywords.add("귀리죽");
        }

        if (foodName.contains("요거트") || foodName.contains("요구르트")) {
            keywords.add("그릭요거트");
            keywords.add("요구르트");
        }

        if (foodName.contains("김치찌개") && !foodName.contains("묵은지")) {
            keywords.add("묵은지 김치찌개");
        }

        if (foodName.contains("묵은지") && foodName.contains("김치찌개")) {
            keywords.add("김치찌개");
        }

        return keywords.stream()
                .map(String::trim)
                .filter(keyword -> !keyword.isBlank())
                .distinct()
                .toList();
    }
}
