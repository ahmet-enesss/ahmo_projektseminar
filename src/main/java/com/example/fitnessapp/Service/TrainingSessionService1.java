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

    //Erstellt eine neue Trainingssession mit Validierungen
    public TrainingSession1 createTrainingSession(Long planId, String name, java.time.LocalDate scheduledDate,
                                                  TrainingSessionStatus status, Set<Long> exerciseIds) {
        //Prüft ob wichtige Felder fehlen
        if (name == null || name.isBlank() || scheduledDate == null || planId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }
       //Prüft ob die Session bereits existiert
        if (trainingSessionRepository.findByNameAndScheduledDateAndTrainingPlan_Id(name, scheduledDate, planId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TrainingSession duplicate for plan/date/name");
        }
        //Holt den Trainingsplan oder wirft 404
        TrainingPlan1 plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingPlan not found"));

        //Lädt alle Übungen basierend ihrer IDs
        Set<Exercise1> exercises = new HashSet<>();
        if (exerciseIds != null) {
            for (Long id : exerciseIds) {
                exercises.add(exerciseRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise with ID " + id + " not found")));
            }
        }

        TrainingSessionStatus finalStatus = status != null ? status : TrainingSessionStatus.GEPLANT;
        //Erstellt ein neues Trainingssession-Objekt und speichert es in der Datenbank
        TrainingSession1 session = TrainingSession1.builder()
                .trainingPlan(plan)
                .name(name)
                .scheduledDate(scheduledDate)
                .status(finalStatus)
                .exerciseExecutions(exercises)
                .build();

        return trainingSessionRepository.save(session);
    }

    public TrainingSession1 updateTrainingSession(Long sessionId, Long planId, String name,
                                                  java.time.LocalDate scheduledDate,
                                                  TrainingSessionStatus status, Set<Long> exerciseIds) {
        //Holt bestehende Session
        TrainingSession1 existing = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingSession not found"));

        //Validierung der Eingaben
        if (name == null || name.isBlank() || scheduledDate == null || planId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }

       //Prüft ob dieselbe Session existiert (mit anderer ID)
        if (trainingSessionRepository
                .findByNameAndScheduledDateAndTrainingPlan_IdAndIdNot(name, scheduledDate, planId, sessionId)
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TrainingSession duplicate for plan/date/name");
        }

      //Holt den zugehörigen Plan
        TrainingPlan1 plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingPlan not found"));

       //Holt Übungen neu
        Set<Exercise1> exercises = new HashSet<>();
        if (exerciseIds != null) {
            for (Long id : exerciseIds) {
                exercises.add(exerciseRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise with ID " + id + " not found")));
            }
        }

        //Aktualisiert die Werte
        existing.setTrainingPlan(plan);
        existing.setName(name);
        existing.setScheduledDate(scheduledDate);
        existing.setStatus(status != null ? status : TrainingSessionStatus.GEPLANT);
        existing.setExerciseExecutions(exercises);

        return trainingSessionRepository.save(existing);
    }

    //Löscht eine Trainingssession anhand der ID
    public void deleteTrainingSession(Long id) {
        trainingSessionRepository.deleteById(id);
    }
}