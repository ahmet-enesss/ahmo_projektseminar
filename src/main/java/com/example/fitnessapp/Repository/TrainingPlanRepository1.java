package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.TrainingPlan1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
// Repository für Datenbankzugriff auf die Klasse TrainingPlan1
public interface TrainingPlanRepository1 extends JpaRepository<TrainingPlan1, Long> {
    Optional<TrainingPlan1> findByName(String name);// sucht nach Trainingspläne anhand der Namen

    Optional<TrainingPlan1> findByNameAndIdNot(String name, Long id);
}