package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.TrainingSession1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingSessionRepository1 extends JpaRepository<TrainingSession1, Long> {
    Optional<TrainingSession1> findByNameAndScheduledDateAndTrainingPlan_Id(String name, java.time.LocalDate scheduledDate, Long trainingPlanId);
}