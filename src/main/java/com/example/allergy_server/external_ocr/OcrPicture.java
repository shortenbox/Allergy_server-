package com.example.allergy_server.external_ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
 * - OCR 통합 처리 서비스
 *
 * [Google Vision]
 * - 일반 사진 OCR
 * - 자연 이미지 OCR
 *
 * [Clova OCR]
 * - 식단표 OCR
 * - 문서 OCR
 * - 표 구조 OCR
 *
 * [특징]
 * - 분석 파이프라인 시작점
 * - MealService 입력 생성기 역할
 * =========================================
 */

@Service
public class OcrPicture {

    @Autowired
    private ClovaOcrClient clovaOcrClient;

    private final String googleKey =
            "AIzaSyDHrF2H8RLss98zW6PEyGWcejp0pu9gr9g";

    private final String googleUrl =
            "https://vision.googleapis.com/v1/images:annotate";

    // =========================
    // 식단표 OCR (Clova)
    // =========================
    public String extractMealText(MultipartFile file) {
        return extractClovaText(file);
    }

    // =========================
    // 일반 사진 OCR (Google)
    // =========================
    public String extractPhotoText(MultipartFile file) {
        return extractGoogleText(file);
    }

    // =========================
    // 기존 호환용
    // =========================
    public String extractText(MultipartFile file) {
        return extractMealText(file);
    }

    // =========================
    // Google OCR
    // =========================
    public String extractGoogleText(MultipartFile file) {
        return callGoogleVision(file);
    }

    // =========================
    // Google Vision 실제 처리
    // =========================
    private String callGoogleVision(MultipartFile file) {

        try {

            String base64 =
                    Base64.getEncoder()
                            .encodeToString(file.getBytes());

            URL apiUrl =
                    new URL(googleUrl + "?key=" + googleKey);

            HttpURLConnection conn =
                    (HttpURLConnection) apiUrl.openConnection();

            conn.setRequestMethod("POST");

            conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

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

                os.write(
                        body.getBytes(StandardCharsets.UTF_8)
                );
            }

            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream(),
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder sb = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root =
                    mapper.readTree(sb.toString());

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
    // Clova OCR
    // =========================
    public String extractClovaText(MultipartFile file) {

        try {

            String response =
                    clovaOcrClient.requestOcr(file);

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root =
                    mapper.readTree(response);

            JsonNode fields =
                    root.path("images")
                            .get(0)
                            .path("fields");

            StringBuilder sb = new StringBuilder();

            for (JsonNode field : fields) {

                String text =
                        field.path("inferText").asText();

                sb.append(text).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {

            e.printStackTrace();

            return "";
        }
    }
}
