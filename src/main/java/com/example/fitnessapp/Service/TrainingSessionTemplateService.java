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
import com.example.fitnessapp.Repository.TrainingPlanSessionTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final TrainingPlanSessionTemplateRepository planTemplateRepository; // may be null for backward compatibility

    // Neuer Konstruktor mit planTemplateRepository
    @Autowired
    public TrainingSessionTemplateService(
            TrainingSessionRepository1 sessionRepository,
            TrainingPlanRepository1 planRepository,
            ExerciseExecutionTemplateRepository exerciseTemplateRepository,
            SessionLogRepository sessionLogRepository,
            TrainingPlanSessionTemplateRepository planTemplateRepository) {
        this.sessionRepository = sessionRepository;
        this.planRepository = planRepository;
        this.exerciseTemplateRepository = exerciseTemplateRepository;
        this.sessionLogRepository = sessionLogRepository;
        this.planTemplateRepository = planTemplateRepository;
    }

    // Überladener Konstruktor (abwärtskompatibel falls Tests alten Konstruktor verwenden)
    public TrainingSessionTemplateService(
            TrainingSessionRepository1 sessionRepository,
            TrainingPlanRepository1 planRepository,
            ExerciseExecutionTemplateRepository exerciseTemplateRepository,
            SessionLogRepository sessionLogRepository) {
        this(sessionRepository, planRepository, exerciseTemplateRepository, sessionLogRepository, null);
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

        // Neue Validierung: globaler eindeutiger Name
        if (sessionRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Es existiert bereits eine Session mit demselben Namen");
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

        // Neue Validierung: globaler eindeutiger Name (außer für diese Session)
        if (sessionRepository.findByNameAndIdNot(request.getName(), id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Es existiert bereits eine Session mit demselben Namen");
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

        // Lösche Join-Links (falls vorhanden), um FK-Probleme zu vermeiden
        if (planTemplateRepository != null) {
            planTemplateRepository.deleteByTrainingSession_Id(id);
        }

        // Lösche ExerciseExecutionTemplates dieser Session
        List<ExerciseExecutionTemplate> exercises = exerciseTemplateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(id);
        if (!exercises.isEmpty()) {
            exerciseTemplateRepository.deleteAll(exercises);
        }

        // Session löschen
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
