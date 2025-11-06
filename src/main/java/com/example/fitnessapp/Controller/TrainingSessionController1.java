package com.example.fitnessapp.Controller;


import com.example.fitnessapp.DTOs.TrainingSessionRequest;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Service.TrainingSessionService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
        return ResponseEntity.ok(trainingSessionService.getTrainingSessionById(id));
    }

    @PostMapping
    public ResponseEntity<TrainingSession1> createTrainingSession(@Valid @RequestBody TrainingSessionRequest request) {
        TrainingSession1 created = trainingSessionService.createTrainingSession(
                request.getPlanId(),
                request.getName(),
                request.getScheduledDate(),
                request.getExerciseIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingSession1> updateTrainingSession(@PathVariable Long id,
                                                                  @Valid @RequestBody TrainingSessionRequest request) {
        TrainingSession1 updated = trainingSessionService.updateTrainingSession(
                id,
                request.getPlanId(),
                request.getName(),
                request.getScheduledDate(),
                request.getExerciseIds()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingSession(@PathVariable Long id) {
        trainingSessionService.deleteTrainingSession(id);
        return ResponseEntity.noContent().build();
    }
}