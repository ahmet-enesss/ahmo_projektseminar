package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.TrainingPlan1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingPlanRepository1 extends JpaRepository<TrainingPlan1, Long> {
    Optional<TrainingPlan1> findByName(String name);
}