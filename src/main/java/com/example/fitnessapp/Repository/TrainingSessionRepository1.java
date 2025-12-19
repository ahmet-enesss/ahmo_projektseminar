package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.TrainingSession1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainingSessionRepository1 extends JpaRepository<TrainingSession1, Long> {

    //Sucht nach einer Session mit gleicher Reihenfolge und Plan (für Eindeutigkeit)
    Optional<TrainingSession1> findByTrainingPlan_IdAndOrderIndex(Long planId, Integer orderIndex);
    
    //Gleiche Abfrage aber mit zusätzlicher ID Prüfung (für Update)
    Optional<TrainingSession1> findByTrainingPlan_IdAndOrderIndexAndIdNot(Long planId, Integer orderIndex, Long id);

    //Alle Sessions eines Plans nach Reihenfolge sortiert
    List<TrainingSession1> findByTrainingPlan_IdOrderByOrderIndexAsc(Long trainingPlanId);

    //Anzahl Sessions pro Plan
    long countByTrainingPlan_Id(Long trainingPlanId);
    
    //Alle Sessions (auch ohne Plan)
    List<TrainingSession1> findAllByOrderByIdAsc();
    
    //Anzahl SessionLogs für eine Session-Vorlage
    @Query("SELECT COUNT(sl) FROM SessionLog sl WHERE sl.templateSession.id = :sessionId")
    long countExecutionsBySessionId(@Param("sessionId") Long sessionId);

    // Neu: Suche nach Name (globale Einzigartigkeit)
    Optional<TrainingSession1> findByName(String name);
    Optional<TrainingSession1> findByNameAndIdNot(String name, Long id);
}