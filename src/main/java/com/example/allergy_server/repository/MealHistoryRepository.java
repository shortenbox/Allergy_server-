package com.example.allergy_server.repository;

import com.example.allergy_server.entity.MealHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 분석 기록 조회/저장을 담당하는 JPA Repository.
 */
public interface MealHistoryRepository extends JpaRepository<MealHistory, Long> {

    // 최근 분석 기록 20개를 최신순으로 조회한다.
    List<MealHistory> findTop20ByOrderByCreatedAtDesc();
}
