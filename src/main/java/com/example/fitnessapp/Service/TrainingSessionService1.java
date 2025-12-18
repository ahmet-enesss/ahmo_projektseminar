package com.example.fitnessapp.Service;

import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Model.TrainingSessionStatus;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
@Service
public class TrainingSessionService1 {

    @Autowired
    private TrainingSessionRepository1 trainingSessionRepository;
    @Autowired
    private TrainingPlanRepository1 trainingPlanRepository;
    @Autowired
    private ExerciseRepository1 exerciseRepository;

    //Holt alle gespeicherten Trainingssessions
    public List<TrainingSession1> getAllTrainingSessions() {
        return trainingSessionRepository.findAll();
    }

    //Holt eine Trainingssession anhand der ID oder gibt 404 zurück
    public TrainingSession1 getTrainingSessionById(Long id) {
        return trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingSession not found"));
    }

    // DEPRECATED: Diese Methoden verwenden das alte Modell. Bitte verwenden Sie TrainingSessionTemplateService stattdessen.
    // Diese Methoden bleiben für Rückwärtskompatibilität, funktionieren aber nicht mehr korrekt.
    @Deprecated
    public TrainingSession1 createTrainingSession(Long planId, String name, java.time.LocalDate scheduledDate,
                                                  TrainingSessionStatus status, Set<Long> exerciseIds) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Diese Methode ist veraltet. Bitte verwenden Sie /api/session-templates stattdessen.");
    }

    @Deprecated
    public TrainingSession1 updateTrainingSession(Long sessionId, Long planId, String name,
                                                  java.time.LocalDate scheduledDate,
                                                  TrainingSessionStatus status, Set<Long> exerciseIds) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "Diese Methode ist veraltet. Bitte verwenden Sie /api/session-templates stattdessen.");
    }

    //Löscht eine Trainingssession anhand der ID
    public void deleteTrainingSession(Long id) {
        trainingSessionRepository.deleteById(id);
    }
}