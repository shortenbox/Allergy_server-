package com.example.allergy_server.controller;

import com.example.allergy_server.service.MealService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * =========================================
 * FoodImageController
 * =========================================
 * [역할]
 * - 일반 음식 이미지 분석 API 처리
 * - 음식 사진 기반 OCR 및 음식 추출 수행
 * - MealService.processImage() 연결
 *
 * [기능]
 * - 음식 사진 업로드 처리
 * - OCR 텍스트 추출
 * - 음식 이름 자동 추출
 * - 음식별 영양 및 알러지 분석
 *
 * [특징]
 * - 일반 음식 사진 전용
 * - 급식표 구조에 의존하지 않음
 * - 햄버거, 라면 등 단일 음식 분석 가능
 *
 * [처리 흐름]
 * 음식 이미지 업로드
 * → OCR 분석
 * → 음식 이름 추출
 * → MealService.processImage()
 * → 분석 결과 반환
 * =========================================
 */


@RestController
@RequestMapping("/api/food")
public class FoodImageController {

    private final MealService mealService;

    public FoodImageController(MealService mealService) {
        this.mealService = mealService;
    }

    // =========================
    // 음식 이미지 분석 API
    // =========================
    @PostMapping("/analyze/image")
    public Map<String, Object> analyzeFoodImage(
            @RequestParam("image") MultipartFile image
    ) {

        return mealService.processImage(image);
    }
}
