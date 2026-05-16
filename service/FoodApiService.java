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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FoodApiService {

    private final String apiKey = "fdca8503ce144d0088e5";

    private final String baseUrl =
            "http://openapi.foodsafetykorea.go.kr/api/";

    private final DataSource dataSource;

    // =========================
    // 🔥 캐시 추가 (핵심)
    // =========================

    private final Map<String, Map<String, Object>> analyzeCache =
            new ConcurrentHashMap<>();

    private final Map<String, String> recipeCache =
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

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("foodName", foodName);

        try {
            String encoded = URLEncoder.encode(foodName, StandardCharsets.UTF_8);

            String urlStr =
                    baseUrl + apiKey +
                            "/COOKRCP01/json/1/5/RCP_NM=" + encoded;

            String response = request(urlStr);

            if (!response.contains("RCP_NM")) {

                String risk = checkAllergy(foodName);

                result.put("risk", risk);

                analyzeCache.put(foodName, result);
                return result;
            }

            result.put("carbohydrate", extract(response, "INFO_CAR"));
            result.put("protein", extract(response, "INFO_PRO"));
            result.put("fat", extract(response, "INFO_FAT"));
            result.put("sodium", extract(response, "INFO_NA"));

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
            String encoded = URLEncoder.encode(foodName, StandardCharsets.UTF_8);

            String urlStr =
                    baseUrl + apiKey +
                            "/COOKRCP01/json/1/5/RCP_NM=" + encoded;

            String response = request(urlStr);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode row = root.path("COOKRCP01").path("row");

            if (!row.isArray() || row.size() == 0) {
                recipeCache.put(foodName, "");
                return "";
            }

            String parts =
                    row.get(0).path("RCP_PARTS_DTLS").asText("");

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
}