package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.TrainingPlanSessionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingPlanSessionTemplateRepository extends JpaRepository<TrainingPlanSessionTemplate, Long> {
    List<TrainingPlanSessionTemplate> findByTrainingPlan_IdOrderByPositionAsc(Long planId);
    Optional<TrainingPlanSessionTemplate> findByTrainingPlan_IdAndTrainingSession_Id(Long planId, Long sessionId);
    long countByTrainingPlan_Id(Long planId);

    // Neu: Links nach SessionId
    List<TrainingPlanSessionTemplate> findByTrainingSession_Id(Long sessionId);
    void deleteByTrainingSession_Id(Long sessionId);
}
