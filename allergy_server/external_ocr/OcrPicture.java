
// 구글 Vision Api 분석
package com.example.allergy_server.external_ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * =========================================
 * OcrPicture
 * =========================================
 * [역할]
 * - 이미지 → 텍스트 변환 (OCR 처리)
 *
 * [성능]
 * - 외부 OCR API 사용 시 가장 느린 단계 중 하나
 * - 이미지 크기 / 품질에 따라 성능 차이 큼
 *
 * [특징]
 * - 분석 파이프라인의 시작점
 * - MealService 입력 생성기 역할
 *
 * [성능 개선 포인트]
 * - 이미지 전처리 (resize / noise removal)
 * - OCR 결과 캐싱
 * =========================================
 */

@Service
public class OcrPicture {

    private final String googleKey = "AIzaSyDHrF2H8RLss98zW6PEyGWcejp0pu9gr9g";
    private final String googleUrl = "https://vision.googleapis.com/v1/images:annotate";

    // =========================
    // 1. 기본 OCR (Google Vision)
    // =========================
    public String extractText(MultipartFile file) {
        return callGoogleVision(file);
    }

    public String extractGoogleText(MultipartFile file) {
        return callGoogleVision(file);
    }

    // =========================
    // 2. Google Vision 실제 처리
    // =========================
    private String callGoogleVision(MultipartFile file) {

        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            URL apiUrl = new URL(googleUrl + "?key=" + googleKey);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = """
            {
              "requests": [{
                "image": { "content": "%s" },
                "features": [
                  { "type": "DOCUMENT_TEXT_DETECTION" }
                ]
              }]
            }
            """.formatted(base64);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(sb.toString());

            return root.path("responses")
                    .get(0)
                    .path("fullTextAnnotation")
                    .path("text")
                    .asText("");

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // =========================
    // 3. Clova (임시 placeholder)
    // =========================
    public String extractClovaText(MultipartFile file) {
        // 나중에 Clova API 연결
        return "CLOVA_NOT_IMPLEMENTED";
    }
}
