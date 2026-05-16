package com.example.allergy_server.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;

/**
 * =========================================
 * CheckAllergyService
 * =========================================
 * [역할]
 * - DB 기반 알러지 위험 판정
 * - ingredient_alias → allergen_in 매핑 처리
 *
 * [성능]
 * - DB 조회 1~2회 수준 → 비교적 빠름
 * - 다만 요청 증가 시 DB 부하 가능
 *
 * [특징]
 * - 알러지 판정 핵심 로직
 * - 시스템 안전성 결정 요소
 *
 * [성능 개선 포인트]
 * - alias / allergen 캐싱 가능
 * =========================================
 */

@Service
public class CheckAllergyService {

    private final String dbUrl =
            "jdbc:mysql://localhost:3306/infant_meal_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul";
    private final String dbUser = "root";
    private final String dbPassword = "1234";

    // =========================
    // 1. 단일 음식 검사 (기존 유지)
    // =========================
    public String check(String foodName) {

        try (Connection conn =
                     DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

            String standard = foodName;

            // alias 변환
            String aliasSql =
                    "SELECT standard_name FROM ingredient_alias WHERE alias_name = ?";

            try (PreparedStatement stmt = conn.prepareStatement(aliasSql)) {
                stmt.setString(1, foodName);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    standard = rs.getString("standard_name");
                }
            }

            // 알러지 체크
            String sql =
                    "SELECT 1 FROM allergen_in WHERE allergen_name = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, standard);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return "위험";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "안전";
    }

    // =========================
    // 2. 확장 버전 (ingredients 포함)
    // =========================
    public String check(String foodName, List<String> ingredients) {

        System.out.println("CHECK FOOD = " + foodName);
        System.out.println("CHECK INGREDIENTS = " + ingredients);

        // 1. 음식 자체 검사
        String result = check(foodName);

        if ("위험".equals(result)) {
            return "위험";
        }

        // 2. 재료 검사
        if (ingredients != null) {
            for (String ing : ingredients) {

                String r = check(ing);

                if ("위험".equals(r)) {
                    return "위험";
                }
            }
        }

        return "안전";
    }
}
