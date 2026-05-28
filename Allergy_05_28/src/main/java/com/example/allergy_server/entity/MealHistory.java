package com.example.allergy_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 이미지 분석 기록을 저장하는 Entity.
 * 업로드 이미지 경로, OCR 원문, 최종 분석 JSON을 함께 보관한다.
 */
@Entity
@Table(name = "meal_history")
public class MealHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 서버에 저장된 업로드 이미지 파일 경로
    @Lob
    @Column(name = "image_path", columnDefinition = "TEXT")
    private String imagePath;

    // OCR 단계에서 추출된 원문 텍스트
    @Lob
    @Column(name = "ocr_text", columnDefinition = "LONGTEXT")
    private String ocrText;

    // 클라이언트에 반환한 최종 분석 결과 JSON 문자열
    @Lob
    @Column(name = "analysis_json", columnDefinition = "LONGTEXT")
    private String analysisJson;

    // DB 기본값과 JPA 저장 시점을 모두 맞추기 위한 생성 시간
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        // JPA로 저장할 때 createdAt이 비어 있으면 현재 시각을 넣는다.
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }

    public String getAnalysisJson() {
        return analysisJson;
    }

    public void setAnalysisJson(String analysisJson) {
        this.analysisJson = analysisJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
