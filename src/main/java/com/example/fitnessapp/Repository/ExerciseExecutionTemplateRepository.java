package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseExecutionTemplateRepository extends JpaRepository<ExerciseExecutionTemplate, Long> {

    List<ExerciseExecutionTemplate> findByTrainingSession_IdOrderByOrderIndexAsc(Long trainingSessionId);

    boolean existsByTrainingSession_IdAndOrderIndex(Long trainingSessionId, Integer orderIndex);
}


