package com.example.allergy_server.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * =========================================
 * IngredientAliasService
 * =========================================
 * [역할]
 * - 음식 이름 표준화 (alias → standard_name 변환)
 * - 사용자 입력 / OCR 결과 정규화
 *
 * [성능]
 * - DB 조회 기반 → 중간 속도
 * - 호출 빈도 매우 높음 (핵심 병목 가능)
 *
 * [특징]
 * - 시스템 정확도 핵심 요소
 * - “달걀후라이 → 계란” 같은 정규화 담당
 *
 * [성능 개선 포인트]
 * - in-memory 캐싱 필수 권장
 * - fuzzy matching 확장 가능
 * =========================================
 */

@Service
public class IngredientAliasService {

    private final DataSource dataSource;

    public IngredientAliasService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * alias → standard_name 변환
     * DB 기반 매핑
     */
    public String convert(String foodName) {

        if (foodName == null || foodName.isBlank()) {
            return "";
        }

        try (Connection conn = dataSource.getConnection()) {

            String sql =
                    "SELECT standard_name " +
                            "FROM ingredient_alias " +
                            "WHERE alias_name = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, foodName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("standard_name");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 없으면 원본 유지
        return foodName;
    }
}
