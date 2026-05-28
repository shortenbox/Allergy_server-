package com.example.allergy_server.service;

import com.example.allergy_server.entity.MealHistory;
import com.example.allergy_server.repository.MealHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 이미지 분석 기록 저장 및 조회 서비스.
 * 이미지 파일은 로컬 uploads 폴더에 저장하고, 분석 결과는 DB에 JSON 문자열로 보관한다.
 */
@Service
public class MealHistoryService {

    private final MealHistoryRepository mealHistoryRepository;
    private final ObjectMapper objectMapper;

    // 프로젝트 실행 위치 기준 uploads/meal-history 폴더에 업로드 이미지를 저장한다.
    private final Path uploadRoot =
            Paths.get("uploads", "meal-history").toAbsolutePath().normalize();

    public MealHistoryService(MealHistoryRepository mealHistoryRepository,
                              ObjectMapper objectMapper) {
        this.mealHistoryRepository = mealHistoryRepository;
        this.objectMapper = objectMapper;
    }

    public MealHistory save(MultipartFile image,
                            String ocrText,
                            Map<String, Object> analysisResult) {
        try {
            // 이미지 파일 경로, OCR 텍스트, 최종 분석 JSON을 하나의 기록으로 저장한다.
            MealHistory history = new MealHistory();
            history.setImagePath(saveImage(image));
            history.setOcrText(ocrText == null ? "" : ocrText);
            history.setAnalysisJson(objectMapper.writeValueAsString(analysisResult));

            return mealHistoryRepository.save(history);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Map<String, Object>> findRecent() {
        // Entity 그대로 반환하지 않고 프론트에서 쓰기 쉬운 응답 Map으로 변환한다.
        return mealHistoryRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Map<String, Object> findById(Long id) {
        return mealHistoryRepository.findById(id)
                .map(this::toResponse)
                .orElseGet(() -> Map.of(
                        "status", "NOT_FOUND",
                        "message", "분석 기록을 찾을 수 없습니다."
                ));
    }

    private String saveImage(MultipartFile image) throws IOException {

        if (image == null || image.isEmpty()) {
            return "";
        }

        // 폴더가 없으면 생성하고, UUID 파일명으로 충돌을 방지한다.
        Files.createDirectories(uploadRoot);

        String originalFilename = image.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String filename = UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve(filename).normalize();

        try (InputStream inputStream = image.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return target.toString();
    }

    private String getExtension(String filename) {

        if (filename == null || filename.isBlank()) {
            return "";
        }

        int dotIndex = filename.lastIndexOf(".");

        if (dotIndex == -1) {
            return "";
        }

        return filename.substring(dotIndex);
    }

    private Map<String, Object> toResponse(MealHistory history) {

        // 저장된 JSON 문자열은 조회 응답에서 다시 JSON 객체 형태로 풀어준다.
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", history.getId());
        response.put("imagePath", history.getImagePath());
        response.put("ocrText", history.getOcrText());
        response.put("analysis", parseAnalysisJson(history.getAnalysisJson()));
        response.put("createdAt", history.getCreatedAt());

        return response;
    }

    private Object parseAnalysisJson(String json) {

        if (json == null || json.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {
                    }
            );

        } catch (Exception e) {
            // 파싱 실패 시에도 기록 자체는 볼 수 있도록 원문 문자열을 반환한다.
            return json;
        }
    }
}
