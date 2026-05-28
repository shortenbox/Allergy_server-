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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * =========================================
 * FoodApiService
 * =========================================
 * [역할]
 * - 식약처 COOKRCP01 API 호출
 * - 영양 정보 및 레시피 데이터 조회
 * - 알러지 DB 체크 보조 수행
 *
 * [성능]
 * - 외부 HTTP API 호출 → 가장 느린 구간 중 하나
 * - 네트워크 latency 영향 큼
 *
 * [특징]
 * - 외부 API 의존 서비스
 * - JSON/XML 파싱 처리 포함
 *
 * [성능 개선 포인트]
 * - API 응답 캐싱 (Redis 추천)
 * - 중복 요청 최소화 필요
 * =========================================
 */

@Service
public class FoodApiService {

    private final String apiKey = "fdca8503ce144d0088e5";

    private final String baseUrl =
            "http://openapi.foodsafetykorea.go.kr/api/";

    private final DataSource dataSource;

    public FoodApiService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // =========================
    // 1. 영양 정보 + 알러지
    // =========================
    public Map<String, Object> analyze(String foodName) {

        Map<String, Object> result = new LinkedHashMap<>();

        if (foodName == null || foodName.isBlank()) {
            result.put("foodName", "");
            result.put("risk", "입력 없음");
            return result;
        }

        foodName = foodName.trim();
        result.put("foodName", foodName);

        try {
            String encoded = URLEncoder.encode(foodName, StandardCharsets.UTF_8);

            String urlStr =
                    baseUrl
                            + apiKey
                            + "/COOKRCP01/json/1/5/RCP_NM="
                            + encoded;

            String response = request(urlStr);

            if (!response.contains("RCP_NM")) {
                result.put("risk", checkAllergy(foodName));
                return result;
            }

            String sodium = extract(response, "INFO_NA");
            String protein = extract(response, "INFO_PRO");
            String fat = extract(response, "INFO_FAT");
            String carb = extract(response, "INFO_CAR");

            result.put("carbohydrate", carb);
            result.put("protein", protein);
            result.put("fat", fat);
            result.put("sodium", sodium);

            result.put("risk", checkAllergy(foodName));

        } catch (Exception e) {
            e.printStackTrace();
            result.put("risk", "안전");
        }

        return result;
    }

    // =========================
    // 2. 재료 추출 (핵심)
    // =========================
    public String getRecipeParts(String foodName) {

        try {
            String encoded = URLEncoder.encode(foodName, StandardCharsets.UTF_8);

            String urlStr =
                    baseUrl
                            + apiKey
                            + "/COOKRCP01/json/1/5/RCP_NM="
                            + encoded;

            String response = request(urlStr);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode row = root.path("COOKRCP01").path("row");

            if (!row.isArray() || row.size() == 0) {
                return "";
            }

            return row.get(0).path("RCP_PARTS_DTLS").asText("");

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // =========================
    // 3. 알러지 체크
    // =========================
    public String checkAllergy(String foodName) {

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
                        return "위험";
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "안전";
    }

    // =========================
    // 4. HTTP 요청
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
    // 5. JSON extract (기존 방식 유지)
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