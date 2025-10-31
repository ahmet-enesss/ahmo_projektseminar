package com.example.fitnessapp.Service;

import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<TrainingSession1> getAllTrainingSessions() {
        return trainingSessionRepository.findAll();
    }

    public TrainingSession1 getTrainingSessionById(Long id) {
        return trainingSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TrainingSession not found"));
    }

    public TrainingSession1 createTrainingSession(Long planId, String name, java.time.LocalDate scheduledDate, Set<Long> exerciseIds) {
        // Prüfen, ob schon vorhanden (Name+Datum+Plan)
        if (trainingSessionRepository.findByNameAndScheduledDateAndTrainingPlan_Id(name, scheduledDate, planId).isPresent()) {
            throw new RuntimeException("TrainingSession mit gleichem Namen und Datum existiert bereits im Plan");
        }
        TrainingPlan1 plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("TrainingPlan nicht gefunden"));

        Set<Exercise1> exercises = new HashSet<>();
        if (exerciseIds != null) {
            for (Long id : exerciseIds) {
                exercises.add(exerciseRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Exercise mit ID " + id + " nicht gefunden")));
            }
        }

        TrainingSession1 session = TrainingSession1.builder()
                .trainingPlan(plan)
                .name(name)
                .scheduledDate(scheduledDate)
                .exerciseExecutions(exercises)
                .build();

        return trainingSessionRepository.save(session);
    }

    public void deleteTrainingSession(Long id) {
        trainingSessionRepository.deleteById(id);
    }
}