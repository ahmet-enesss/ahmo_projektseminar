package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.TrainingSession1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingSessionRepository1 extends JpaRepository<TrainingSession1, Long> {

    //Sucht nach einer Session mit gleichem Namen, Datum und Plan (verwendet um doppelte Einträge zu vermeiden)
    Optional<TrainingSession1> findByNameAndScheduledDateAndTrainingPlan_Id(String name, java.time.LocalDate scheduledDate, Long trainingPlanId);
   //Gleiche Abfrage aber mit zusätzlicher ID Prüfung
    Optional<TrainingSession1> findByNameAndScheduledDateAndTrainingPlan_IdAndIdNot(String name, java.time.LocalDate scheduledDate, Long trainingPlanId, Long id);
}