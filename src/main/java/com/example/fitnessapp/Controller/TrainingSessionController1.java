package com.example.fitnessapp.Controller;


import com.example.fitnessapp.DTOs.TrainingSessionRequest;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Service.TrainingSessionService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainingsessions")
public class TrainingSessionController1 {

    @Autowired
    private TrainingSessionService1 trainingSessionService;

    @GetMapping
    public List<TrainingSession1> getAllTrainingSessions() {
        return trainingSessionService.getAllTrainingSessions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingSession1> getTrainingSessionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(trainingSessionService.getTrainingSessionById(id));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createTrainingSession(@RequestBody TrainingSessionRequest request) {
        try {
            TrainingSession1 created = trainingSessionService.createTrainingSession(
                    request.getPlanId(),
                    request.getName(),
                    request.getScheduledDate(),
                    request.getExerciseIds()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingSession(@PathVariable Long id) {
        trainingSessionService.deleteTrainingSession(id);
        return ResponseEntity.noContent().build();
    }
}