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

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/trainingsessions")
public class TrainingSessionController1 {

    //Verbindung zu der Serviceschicht
    private TrainingSessionService1 trainingSessionService;

    @Autowired
    public TrainingSessionController1(TrainingSessionService1 trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }

    //Gibt eine Liste aller gespeicherten Trainingssessions zurück
    @GetMapping
    public List<TrainingSession1> getAllTrainingSessions() {

        return trainingSessionService.getAllTrainingSessions();
    }

    //Gibt einzelne Trainingssessions anhand der ID zurück
    @GetMapping("/{id}")
    public ResponseEntity<TrainingSession1> getTrainingSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingSessionService.getTrainingSessionById(id));
    }

    //Erstellt eine neue Trainingssession
    @PostMapping
    public ResponseEntity<TrainingSession1> createTrainingSession(@Valid @RequestBody TrainingSessionRequest request) {
       //Übergibt die Werte aus dem Request an den Service wieder
        TrainingSession1 created = trainingSessionService.createTrainingSession(
                request.getPlanId(),
                request.getName(),
                request.getScheduledDate(),
                request.getStatus(),
                request.getExerciseIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created); //Gibt neu erstellte Session mit HTTP 201 zurück
    }

    //Aktualisiert eine vorhandene Trainingssession anhand ihrer ID wieder
    @PutMapping("/{id}")
    public ResponseEntity<TrainingSession1> updateTrainingSession(@PathVariable Long id,
                                                                  @Valid @RequestBody TrainingSessionRequest request) {
        TrainingSession1 updated = trainingSessionService.updateTrainingSession(
                id,
                request.getPlanId(),
                request.getName(),
                request.getScheduledDate(),
                request.getStatus(),
                request.getExerciseIds()
        );
        return ResponseEntity.ok(updated);
    }

    //Löscht eine Trainingssession anhand der ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingSession(@PathVariable Long id) {
        trainingSessionService.deleteTrainingSession(id);
        return ResponseEntity.noContent().build(); //Gibt 204 No Content zurück, wenn die Löschung natürlich erfolgreich war
    }
}