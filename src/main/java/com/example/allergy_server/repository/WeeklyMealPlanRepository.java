package com.example.allergy_server.repository;

import com.example.allergy_server.entity.WeeklyMealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 날짜별 식단표 저장/조회 Repository.
 */
public interface WeeklyMealPlanRepository extends JpaRepository<WeeklyMealPlan, Long> {

    Optional<WeeklyMealPlan> findByPlanDate(LocalDate planDate);

    List<WeeklyMealPlan> findByPlanDateBetweenOrderByPlanDateAsc(
            LocalDate startDate,
            LocalDate endDate
    );
}
