package com.example.allergy_server.controller;

import com.example.allergy_server.parser.MealTextParser;
import com.example.allergy_server.service.MealHistoryService;
import com.example.allergy_server.service.MealService;

import com.example.allergy_server.external_ocr.OcrPicture;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final MealHistoryService mealHistoryService;

    public OcrController(OcrPicture ocrPicture,
                         MealService mealService,
                         MealHistoryService mealHistoryService) {
        this.ocrPicture = ocrPicture;
        this.mealService = mealService;
        this.mealHistoryService = mealHistoryService;
    }

    // =========================
    // 1️⃣ 이미지 기반 분석 API
    // =========================
    @PostMapping("/analyze/image")
    public Map<String, Object> analyzeImage(
            @RequestParam MultipartFile image,
            @RequestParam(required = false) String type
    ) {
        String text = ocrPicture.extractText(image);
        Map<String, String> meals = MealTextParser.parse(text);

        if (!meals.isEmpty()) {
            Map<String, Object> result = mealService.analyzeMeal(meals);
            // 식단표 분석 결과를 이미지/OCR 원문과 함께 기록으로 저장한다.
            mealHistoryService.save(image, text, result);
            return result;
        }

        Map<String, Object> result = mealService.processImage(image);
        // 식단표가 아닌 단일 메뉴 이미지 분석 결과도 동일한 기록 테이블에 저장한다.
        mealHistoryService.save(image, getOcrText(result), result);
        return result;
    }

    private String getOcrText(Map<String, Object> result) {
        Object ocrText = result.get("ocrText");
        return ocrText == null ? "" : ocrText.toString();
    }
}
