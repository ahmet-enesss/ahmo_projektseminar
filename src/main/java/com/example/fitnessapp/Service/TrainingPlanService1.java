package com.example.fitnessapp.Service;


import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service //markiert die Klasse als Service-Komponente
public class TrainingPlanService1 {

    @Autowired
    private TrainingPlanRepository1 trainingPlanRepository;//Zugriff auf Trainingsrepository
    // Methode gibt alle Trainingspläne zurück
    public List<TrainingPlan1> getAllTrainingPlans() {
        return trainingPlanRepository.findAll();
    }
    // Methode sucht Trainingsplan nach seiner ID
    public TrainingPlan1 getTrainingPlanById(Long id) {
        return trainingPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TrainingPlan not found"));
    }
    // Methode erstellt neuen Trainingsplan
    public TrainingPlan1 createTrainingPlan(TrainingPlan1 plan) {
        if (trainingPlanRepository.findByName(plan.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TrainingPlan with this name already exists");
        }
        return trainingPlanRepository.save(plan);
    }
}