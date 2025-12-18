package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseExecutionTemplateRepository extends JpaRepository<ExerciseExecutionTemplate, Long> { // Spring Data JPA Repository für die ExerciseExecutionTemplate-Entity
// Bietet CRUD-Operationen und benutzerdefinierte Abfragen

    List<ExerciseExecutionTemplate> findByTrainingSession_IdOrderByOrderIndexAsc(Long trainingSessionId);  // Liefert alle ExerciseExecutionTemplates einer Trainingssession, sortiert nach der Reihenfolge

    boolean existsByTrainingSession_IdAndOrderIndex(Long trainingSessionId, Integer orderIndex); // Prüft, ob bereits ein Template mit derselben Reihenfolge in der Session existiert

    boolean existsByTrainingSession_IdAndExercise_Id(Long trainingSessionId, Long exerciseId); // Prüft, ob eine Übung bereits in der Session existiert
}
