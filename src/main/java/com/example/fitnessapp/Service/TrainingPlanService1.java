package com.example.fitnessapp.Service;


import com.example.fitnessapp.DTOs.TrainingPlanDetailResponse;
import com.example.fitnessapp.DTOs.TrainingPlanOverviewResponse;
import com.example.fitnessapp.DTOs.TrainingSessionSummaryResponse;
import com.example.fitnessapp.DTOs.TrainingPlanRequest;
import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Model.TrainingPlanSessionTemplate;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.TrainingPlanSessionTemplateRepository;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
    @Autowired
    private TrainingPlanSessionTemplateRepository planTemplateRepository;
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

        // Hole die verknüpften Session-Vorlagen über die Link-Tabelle und sortiere nach Position
        List<TrainingSessionSummaryResponse> sessions = planTemplateRepository
                .findByTrainingPlan_IdOrderByPositionAsc(id)
                .stream()
                .map(link -> {
                    TrainingSession1 sess = link.getTrainingSession();
                    int exerciseCount = exerciseTemplateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(sess.getId()).size();
                    return TrainingSessionSummaryResponse.builder()
                            .id(sess.getId())
                            .name(sess.getName())
                            .orderIndex(link.getPosition() == null ? sess.getOrderIndex() : link.getPosition())
                            .exerciseCount(exerciseCount)
                            .build();
                })
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

    // Fügt eine Session-Vorlage als Referenz in einen Plan ein. Wenn position null, wird ans Ende gehängt.
    public void addTemplateToPlan(Long planId, Long templateId, Integer position) {
        TrainingPlan1 plan = trainingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingPlan not found"));
        TrainingSession1 sessionTemplate = trainingSessionRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingSession template not found"));

        // Prüfen ob Link bereits existiert
        if (planTemplateRepository.findByTrainingPlan_IdAndTrainingSession_Id(planId, templateId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Template already added to plan");
        }

        List<TrainingPlanSessionTemplate> existing = planTemplateRepository.findByTrainingPlan_IdOrderByPositionAsc(planId);
        // Normalisiere Positionen (1..n) entsprechend der aktuellen Reihenfolge
        for (int i = 0; i < existing.size(); i++) {
            TrainingPlanSessionTemplate l = existing.get(i);
            l.setPosition(i + 1);
        }
        planTemplateRepository.saveAll(existing);

        int insertPos = (position == null) ? existing.size() + 1 : Math.max(1, Math.min(position, existing.size() + 1));

        // Verschiebe nachfolgende Positionen um +1
        for (TrainingPlanSessionTemplate link : existing) {
            if (link.getPosition() >= insertPos) {
                link.setPosition(link.getPosition() + 1);
            }
        }
        planTemplateRepository.saveAll(existing);

        TrainingPlanSessionTemplate newLink = TrainingPlanSessionTemplate.builder()
                .trainingPlan(plan)
                .trainingSession(sessionTemplate)
                .position(insertPos)
                .build();
        planTemplateRepository.save(newLink);
    }

    // Entfernt eine Session-Vorlage aus dem Plan und rückt Positionen nach
    public void removeTemplateFromPlan(Long planId, Long templateId) {
        TrainingPlanSessionTemplate link = planTemplateRepository.findByTrainingPlan_IdAndTrainingSession_Id(planId, templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template link not found in plan"));
        planTemplateRepository.delete(link);

        // Neu: verbleibende Links sequenziell neu nummerieren
        List<TrainingPlanSessionTemplate> remaining = planTemplateRepository.findByTrainingPlan_IdOrderByPositionAsc(planId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i + 1);
        }
        planTemplateRepository.saveAll(remaining);
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