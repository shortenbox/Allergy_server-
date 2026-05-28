package com.example.allergy_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * =========================================
 * AllergyServerApplication
 * =========================================
 * [역할]
 * - Spring Boot 애플리케이션 실행 진입점
 * - 전체 Bean 초기화 및 서버 구동 담당
 *
 * [성능]
 * - 단순 부트스트랩 클래스 → 성능 영향 없음
 * - 요청 처리 로직 없음
 *
 * [특징]
 * - 시스템 시작점 (Entry Point)
 * =========================================
 */

@SpringBootApplication
public class AllergyServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllergyServerApplication.class, args);
    }
}