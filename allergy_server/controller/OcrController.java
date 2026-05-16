package com.example.allergy_server.controller;

import com.example.allergy_server.service.MealService;
import com.example.allergy_server.parser.MealTextParser;

import com.example.allergy_server.external_ocr.OcrPicture;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * =========================================
 * OcrController
 * =========================================
 * [역할]
 * - OCR 기반 식단표 이미지 분석 API 처리
 * - 이미지 업로드 및 OCR 결과 분석 수행
 * - OCR 결과를 MealService와 연결
 *
 * [기능]
 * - /api/meal/analyze/image
 * - 이미지 파일 업로드 처리
 * - Google Vision OCR 호출
 * - 급식표 텍스트 분석
 *
 * [특징]
 * - 이미지 입력 전용 API
 * - OCR 처리 흐름 담당
 * - 식단표 형태 분석에 최적화
 *
 * [처리 흐름]
 * 이미지 업로드
 * → OCR 텍스트 추출
 * → MealTextParser 분석
 * → MealService 호출
 * → 결과 JSON 반환
 * =========================================
 */

@RestController
@RequestMapping("/api/meal")
public class OcrController {

    private final OcrPicture ocrPicture;
    private final MealService mealService;

    public OcrController(OcrPicture ocrPicture,
                         MealService mealService) {
        this.ocrPicture = ocrPicture;
        this.mealService = mealService;
    }

    // =========================
    // 1️⃣ 이미지 기반 분석 API
    // =========================
    @PostMapping("/analyze/image")
    public Map<String, Object> analyzeImage(
            @RequestParam MultipartFile image,
            @RequestParam(required = false) String type
    ) {

        try {
            // 1. OCR
            String text = ocrPicture.extractText(image);

            // 2. 텍스트 → 식단 분해
            Map<String, String> meals = MealTextParser.parse(text);

            // 3. 분석
            return mealService.analyzeMeal(meals);

        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "이미지 분석 실패");
            return error;
        }
    }

    // =========================
    // 2️⃣ 텍스트 기반 분석 API
    // =========================
    @PostMapping("/analyze/text")
    public Map<String, Object> analyzeText(
            @RequestBody Map<String, String> meals
    ) {

        return mealService.analyzeMeal(meals);
    }
}
