package com.example.fitnessapp.Service;


import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingPlanService1 {

    @Autowired
    private TrainingPlanRepository1 trainingPlanRepository;

    public List<TrainingPlan1> getAllTrainingPlans() {
        return trainingPlanRepository.findAll();
    }

    public TrainingPlan1 getTrainingPlanById(Long id) {
        return trainingPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TrainingPlan not found"));
    }

    public TrainingPlan1 createTrainingPlan(TrainingPlan1 plan) {
        if (trainingPlanRepository.findByName(plan.getName()).isPresent()) {
            throw new RuntimeException("TrainingPlan with this name already exists");
        }
        return trainingPlanRepository.save(plan);
    }
}