package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.TrainingPlanRequest;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Service.TrainingPlanService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainingplans")
public class TrainingPlanController1 {

    @Autowired
    private TrainingPlanService1 trainingPlanService;

    @GetMapping
    public List<TrainingPlan1> getAllTrainingPlans() {
        return trainingPlanService.getAllTrainingPlans();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingPlan1> getTrainingPlanById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(trainingPlanService.getTrainingPlanById(id));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createTrainingPlan(@RequestBody TrainingPlanRequest request) {
        try {
            TrainingPlan1 plan = TrainingPlan1.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();
            TrainingPlan1 created = trainingPlanService.createTrainingPlan(plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}