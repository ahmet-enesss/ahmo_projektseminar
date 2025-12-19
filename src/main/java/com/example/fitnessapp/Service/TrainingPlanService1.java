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
import com.example.fitnessapp.Repository.TrainingPlanSessionTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Service //markiert die Klasse als Service-Komponente
public class TrainingPlanService1 {

    @Autowired
    private TrainingPlanRepository1 trainingPlanRepository;//Zugriff auf Trainingsrepository
    @Autowired
    private TrainingSessionRepository1 trainingSessionRepository;
    @Autowired
    private ExerciseExecutionTemplateRepository exerciseTemplateRepository;
    @Autowired(required = false)
    private TrainingPlanSessionTemplateRepository planTemplateRepository; // optional, Unit-Tests mocken nicht immer
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

    @Transactional
    public void addTemplateToPlan(Long planId, Long templateId, Integer position) {
        // Neue Logik: Erstelle eine Kopie der Session-Vorlage (inkl. ExerciseExecutionTemplate) und füge sie dem Plan hinzu
        TrainingPlan1 plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingPlan not found"));
        TrainingSession1 template = trainingSessionRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session-Template not found"));

        // Erzeuge eindeutigen Namen: falls Name existiert, hänge Suffix an
        String baseName = template.getName();
        String newName = baseName;
        int suffix = 1;
        while (trainingSessionRepository.findByName(newName).isPresent()) {
            newName = baseName + " (Kopie " + suffix + ")";
            suffix++;
        }

        // Bestimme orderIndex: wenn position angegeben, verwende sie (prüfe Konflikt), sonst setze nächsthöhere freie Reihenfolge
        Integer orderIndex = position;
        if (orderIndex == null) {
            long count = trainingSessionRepository.countByTrainingPlan_Id(planId);
            orderIndex = (int) count + 1;
            if (orderIndex > 30) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pro Trainingsplan können maximal 30 Sessions angelegt werden");
            }
        } else {
            // Prüfe ob Reihenfolge bereits existiert
            if (trainingSessionRepository.findByTrainingPlan_IdAndOrderIndex(planId, orderIndex).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Reihenfolge " + orderIndex + " ist bereits für diesen Plan vergeben");
            }
        }

        TrainingSession1 copy = TrainingSession1.builder()
                .name(newName)
                .trainingPlan(plan)
                .orderIndex(orderIndex)
                .build();
        copy = trainingSessionRepository.save(copy);

        // Kopiere ExerciseExecutionTemplates
        List<ExerciseExecutionTemplate> exercises = exerciseTemplateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(templateId);
        for (ExerciseExecutionTemplate et : exercises) {
            ExerciseExecutionTemplate clone = ExerciseExecutionTemplate.builder()
                    .trainingSession(copy)
                    .exercise(et.getExercise())
                    .plannedSets(et.getPlannedSets())
                    .plannedReps(et.getPlannedReps())
                    .plannedWeight(et.getPlannedWeight())
                    .orderIndex(et.getOrderIndex())
                    .build();
            exerciseTemplateRepository.save(clone);
        }
    }

    @Transactional
    public void removeTemplateFromPlan(Long planId, Long templateId) {
        // Wenn Link-Eintrag vorhanden, lösche diesen; ansonsten lösche Session (Kopie)
        if (planTemplateRepository != null) {
            var opt = planTemplateRepository.findByTrainingPlan_IdAndTrainingSession_Id(planId, templateId);
            if (opt.isPresent()) {
                planTemplateRepository.delete(opt.get());
                return;
            }
        }

        TrainingSession1 session = trainingSessionRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (session.getTrainingPlan() == null || !session.getTrainingPlan().getId().equals(planId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session gehört nicht zu diesem Plan");
        }
        // Lösche vorher mögliche ExerciseTemplates
        List<ExerciseExecutionTemplate> exercises = exerciseTemplateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(session.getId());
        exerciseTemplateRepository.deleteAll(exercises);
        trainingSessionRepository.delete(session);
    }

    public void deleteTrainingPlan(Long id) {
        TrainingPlan1 plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingPlan not found"));
        // Lösche ggf. Join-Links
        if (planTemplateRepository != null) {
            List<com.example.fitnessapp.Model.TrainingPlanSessionTemplate> links = planTemplateRepository.findByTrainingPlan_IdOrderByPositionAsc(id);
            if (!links.isEmpty()) {
                planTemplateRepository.deleteAll(links);
            }
        }
        List<TrainingSession1> sessions = trainingSessionRepository.findByTrainingPlan_IdOrderByOrderIndexAsc(id);
        for (TrainingSession1 session : sessions) {
            // lösche zugehörige ExerciseTemplates
            List<ExerciseExecutionTemplate> exercises = exerciseTemplateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(session.getId());
            exerciseTemplateRepository.deleteAll(exercises);
            session.setTrainingPlan(null);
            // falls Kopien im System sind, evtl. löschen oder behalten; hier belassen wir die Sessions aber ohne Plan
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