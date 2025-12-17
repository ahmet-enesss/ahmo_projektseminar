package com.example.fitnessapp.Service;


import com.example.fitnessapp.DTOs.TrainingPlanDetailResponse;
import com.example.fitnessapp.DTOs.TrainingPlanOverviewResponse;
import com.example.fitnessapp.DTOs.TrainingSessionSummaryResponse;
import com.example.fitnessapp.DTOs.TrainingPlanRequest;
import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service //markiert die Klasse als Service-Komponente
public class TrainingPlanService1 {

    @Autowired
    private TrainingPlanRepository1 trainingPlanRepository;//Zugriff auf Trainingsrepository
    @Autowired
    private TrainingSessionRepository1 trainingSessionRepository;
    @Autowired
    private ExerciseExecutionTemplateRepository exerciseTemplateRepository;
    // Methode gibt alle Trainingspläne zurück
    public List<TrainingPlanOverviewResponse> getAllTrainingPlans() {
        return trainingPlanRepository.findAll().stream()
                .map(plan -> TrainingPlanOverviewResponse.builder()
                        .id(plan.getId())
                        .name(plan.getName())
                        .description(plan.getDescription())
                        .sessionCount(trainingSessionRepository.countByTrainingPlan_Id(plan.getId()))
                        .build())
                .collect(Collectors.toList());
    }
    // Methode sucht Trainingsplan nach seiner ID
    public TrainingPlanDetailResponse getTrainingPlanById(Long id) {
        TrainingPlan1 plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingPlan not found"));
        List<TrainingSessionSummaryResponse> sessions = trainingSessionRepository
                .findByTrainingPlan_IdOrderByOrderIndexAsc(id)
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
        boolean hasSessions = !sessions.isEmpty();
        return TrainingPlanDetailResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .sessions(sessions)
                .hasSessions(hasSessions)
                .sessionsHint(hasSessions ? "" : "Noch keine Trainingssessions geplant")
                .build();
    }
    // Methode erstellt neuen Trainingsplan
    public TrainingPlan1 createTrainingPlan(TrainingPlan1 plan) {
        if (trainingPlanRepository.findByName(plan.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TrainingPlan with this name already exists");
        }
        return trainingPlanRepository.save(plan);
    }

    public TrainingPlan1 updateTrainingPlan(Long id, TrainingPlanRequest request) {
        TrainingPlan1 existing = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingPlan not found"));
        if (trainingPlanRepository.findByNameAndIdNot(request.getName(), id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TrainingPlan with this name already exists");
        }
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        return trainingPlanRepository.save(existing);
    }

    public void deleteTrainingPlan(Long id) {
        TrainingPlan1 plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingPlan not found"));
        List<TrainingSession1> sessions = trainingSessionRepository.findByTrainingPlan_IdOrderByOrderIndexAsc(id);
        for (TrainingSession1 session : sessions) {
            session.setTrainingPlan(null);
        }
        trainingSessionRepository.saveAll(sessions);
        trainingPlanRepository.delete(plan);
    }

    private TrainingSessionSummaryResponse mapToSummary(TrainingSession1 session) {
        // Anzahl Übungen über ExerciseExecutionTemplate zählen
        int exerciseCount = exerciseTemplateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(session.getId()).size();
        
        return TrainingSessionSummaryResponse.builder()
                .id(session.getId())
                .name(session.getName())
                .orderIndex(session.getOrderIndex())
                .exerciseCount(exerciseCount)
                .build();
    }
}