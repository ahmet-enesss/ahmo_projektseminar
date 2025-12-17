package com.example.fitnessapp.Service;

import com.example.fitnessapp.DTOs.TrainingSessionTemplateOverviewResponse;
import com.example.fitnessapp.DTOs.TrainingSessionTemplateRequest;
import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.SessionLogRepository;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingSessionTemplateService {

    private final TrainingSessionRepository1 sessionRepository;
    private final TrainingPlanRepository1 planRepository;
    private final ExerciseExecutionTemplateRepository exerciseTemplateRepository;
    private final SessionLogRepository sessionLogRepository;

    public TrainingSessionTemplateService(
            TrainingSessionRepository1 sessionRepository,
            TrainingPlanRepository1 planRepository,
            ExerciseExecutionTemplateRepository exerciseTemplateRepository,
            SessionLogRepository sessionLogRepository) {
        this.sessionRepository = sessionRepository;
        this.planRepository = planRepository;
        this.exerciseTemplateRepository = exerciseTemplateRepository;
        this.sessionLogRepository = sessionLogRepository;
    }

    public List<TrainingSessionTemplateOverviewResponse> getAllSessions() {
        return sessionRepository.findAllByOrderByIdAsc().stream()
                .map(this::toOverviewResponse)
                .collect(Collectors.toList());
    }

    public TrainingSessionTemplateOverviewResponse getSessionById(Long id) {
        TrainingSession1 session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session-Vorlage nicht gefunden"));
        return toOverviewResponse(session);
    }

    @Transactional
    public TrainingSessionTemplateOverviewResponse createSession(TrainingSessionTemplateRequest request) {
        // Validierungen
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name ist erforderlich");
        }
        if (request.getOrderIndex() == null || request.getOrderIndex() < 1 || request.getOrderIndex() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reihenfolge muss zwischen 1 und 30 liegen");
        }

        // Plan prüfen (falls angegeben)
        TrainingPlan1 plan = null;
        if (request.getPlanId() != null) {
            plan = planRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainingsplan nicht gefunden"));

            // Prüfen ob bereits 30 Sessions im Plan existieren
            long sessionCount = sessionRepository.countByTrainingPlan_Id(request.getPlanId());
            if (sessionCount >= 30) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pro Trainingsplan können maximal 30 Sessions angelegt werden");
            }

            // Prüfen ob Reihenfolge bereits existiert
            if (sessionRepository.findByTrainingPlan_IdAndOrderIndex(request.getPlanId(), request.getOrderIndex()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Reihenfolge " + request.getOrderIndex() + " ist bereits für diesen Plan vergeben");
            }
        }

        TrainingSession1 session = TrainingSession1.builder()
                .name(request.getName())
                .trainingPlan(plan)
                .orderIndex(request.getOrderIndex())
                .build();

        session = sessionRepository.save(session);
        return toOverviewResponse(session);
    }

    @Transactional
    public TrainingSessionTemplateOverviewResponse updateSession(Long id, TrainingSessionTemplateRequest request) {
        TrainingSession1 session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session-Vorlage nicht gefunden"));

        // Validierungen
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name ist erforderlich");
        }
        if (request.getOrderIndex() == null || request.getOrderIndex() < 1 || request.getOrderIndex() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reihenfolge muss zwischen 1 und 30 liegen");
        }

        // Plan prüfen (falls angegeben)
        TrainingPlan1 plan = null;
        if (request.getPlanId() != null) {
            plan = planRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainingsplan nicht gefunden"));

            // Prüfen ob Reihenfolge bereits existiert (außer für diese Session)
            if (sessionRepository.findByTrainingPlan_IdAndOrderIndexAndIdNot(request.getPlanId(), request.getOrderIndex(), id).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Reihenfolge " + request.getOrderIndex() + " ist bereits für diesen Plan vergeben");
            }
        }

        session.setName(request.getName());
        session.setTrainingPlan(plan);
        session.setOrderIndex(request.getOrderIndex());

        session = sessionRepository.save(session);
        return toOverviewResponse(session);
    }

    @Transactional
    public void deleteSession(Long id) {
        TrainingSession1 session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session-Vorlage nicht gefunden"));
        
        // SessionLogs bleiben erhalten (Historie bleibt erhalten)
        sessionRepository.delete(session);
    }

    private TrainingSessionTemplateOverviewResponse toOverviewResponse(TrainingSession1 session) {
        // Anzahl Übungen
        int exerciseCount = exerciseTemplateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(session.getId()).size();

        // Anzahl Durchführungen
        long executionCount = sessionLogRepository.countByTemplateSession_Id(session.getId());

        return TrainingSessionTemplateOverviewResponse.builder()
                .id(session.getId())
                .name(session.getName())
                .planId(session.getTrainingPlan() != null ? session.getTrainingPlan().getId() : null)
                .planName(session.getTrainingPlan() != null ? session.getTrainingPlan().getName() : "Kein Plan")
                .orderIndex(session.getOrderIndex())
                .exerciseCount(exerciseCount)
                .executionCount(executionCount)
                .build();
    }
}

