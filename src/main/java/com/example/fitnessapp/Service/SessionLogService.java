package com.example.fitnessapp.Service;

import com.example.fitnessapp.DTOs.*;
import com.example.fitnessapp.Model.*;
import com.example.fitnessapp.Repository.ExecutionLogRepository;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.SessionLogRepository;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionLogService {

    private final SessionLogRepository sessionLogRepository;
    private final ExecutionLogRepository executionLogRepository;
    private final ExerciseExecutionTemplateRepository templateRepository;
    private final TrainingSessionRepository1 trainingSessionRepository;

    public SessionLogService(SessionLogRepository sessionLogRepository, // Konstruktor-Injection der benötigten Repositories
                             ExecutionLogRepository executionLogRepository,
                             ExerciseExecutionTemplateRepository templateRepository,
                             TrainingSessionRepository1 trainingSessionRepository) {
        this.sessionLogRepository = sessionLogRepository;
        this.executionLogRepository = executionLogRepository;
        this.templateRepository = templateRepository;
        this.trainingSessionRepository = trainingSessionRepository;
    }

    public SessionLogDetailResponse start(SessionLogCreateRequest request) { // Startet eine neue Trainingssession basierend auf einem Template
        TrainingSession1 templateSession = trainingSessionRepository.findById(request.getSessionTemplateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingSession template not found"));

        List<ExerciseExecutionTemplate> templates =  // Hole alle Exercise-Templates für diese Session
                templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(templateSession.getId());

        if (templates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "TrainingSession template must contain at least one exercise");
        }

        SessionLog log = SessionLog.builder() // Neues SessionLog erstellen
                .templateSession(templateSession)
                .status(LogStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        log = sessionLogRepository.save(log);

        for (ExerciseExecutionTemplate t : templates) {
            // Für jede Übung im Template einen ExecutionLog anlegen
            ExecutionLog exec = ExecutionLog.builder()
                    .sessionLog(log)
                    .exerciseTemplate(t)
                    .actualSets(t.getPlannedSets())
                    .actualReps(t.getPlannedReps())
                    .actualWeight(t.getPlannedWeight())
                    .completed(false)
                    .notes(null)
                    .build();
            executionLogRepository.save(exec);
        }

        // Nach dem Anlegen der Exercise-Logs erneut laden, damit die Beziehung gefüllt ist
        SessionLog reloaded = sessionLogRepository.findById(log.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Created log not found"));

        return toDetail(reloaded);
    }

    public SessionLogDetailResponse getDetail(Long id) {  // Liefert Details eines SessionLogs inklusive aller ExecutionLogs
        SessionLog log = sessionLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));
        return toDetail(log);
    }

    public ExecutionLogResponse updateExecution(ExecutionLogUpdateRequest request) {  // Aktualisiert einen ExecutionLog während der Session
        ExecutionLog exec = executionLogRepository.findById(request.getExecutionLogId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ExecutionLog not found"));

        SessionLog log = exec.getSessionLog();
        if (log.getStatus() != LogStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Changes only allowed while training is IN_PROGRESS");
        }

        if (request.getActualSets() == null || request.getActualSets() <= 0 ||    // Validierung der tatsächlichen Werte
                request.getActualReps() == null || request.getActualReps() <= 0 ||
                request.getActualWeight() == null || request.getActualWeight() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid actual values");
        }

        exec.setActualSets(request.getActualSets());        // Werte aktualisieren
        exec.setActualReps(request.getActualReps());
        exec.setActualWeight(request.getActualWeight());
        exec.setCompleted(request.getCompleted() != null ? request.getCompleted() : exec.getCompleted());
        exec.setNotes(request.getNotes());

        return toExecutionResponse(executionLogRepository.save(exec));
    }

    public SessionLogSummaryResponse complete(Long logId) { // Markiert ein SessionLog als abgeschlossen
        SessionLog log = sessionLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));

        if (log.getStatus() != LogStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only IN_PROGRESS logs can be completed");
        }

        log.setStatus(LogStatus.COMPLETED);
        log.setEndTime(LocalDateTime.now());

        return toSummary(sessionLogRepository.save(log));
    }

    public void abort(Long logId) {  // Bricht eine Session ab (löscht SessionLog)
        SessionLog log = sessionLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));

        if (log.getStatus() != LogStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only IN_PROGRESS logs can be deleted");
        }

        sessionLogRepository.delete(log);
    }

    private SessionLogSummaryResponse toSummary(SessionLog log) { //Wandelt ein SessionLog in ein Summary-DTO um
        return SessionLogSummaryResponse.builder()
                .id(log.getId())
                .sessionTemplateId(log.getTemplateSession().getId())
                .sessionName(log.getTemplateSession().getName())
                .startTime(log.getStartTime())
                .endTime(log.getEndTime())
                .status(log.getStatus())
                .build();
    }

    private SessionLogDetailResponse toDetail(SessionLog log) { //Wandelt ein SessionLog in ein Detail-DTO um (inklusive ExecutionLogs)
        List<ExecutionLogResponse> executions = log.getExerciseLogs()
                .stream()
                .map(this::toExecutionResponse)
                .collect(Collectors.toList());

        return SessionLogDetailResponse.builder()
                .id(log.getId())
                .sessionTemplateId(log.getTemplateSession().getId())
                .sessionName(log.getTemplateSession().getName())
                .startTime(log.getStartTime())
                .endTime(log.getEndTime())
                .status(log.getStatus())
                .notes(log.getNotes())
                .executions(executions)
                .build();
    }

    private ExecutionLogResponse toExecutionResponse(ExecutionLog exec) { //Wandelt einen ExecutionLog in ein Response-DTO um
        return ExecutionLogResponse.builder()
                .id(exec.getId())
                .exerciseTemplateId(exec.getExerciseTemplate().getId())
                .exerciseName(exec.getExerciseTemplate().getExercise().getName())
                .plannedSets(exec.getExerciseTemplate().getPlannedSets())
                .plannedReps(exec.getExerciseTemplate().getPlannedReps())
                .plannedWeight(exec.getExerciseTemplate().getPlannedWeight())
                .actualSets(exec.getActualSets())
                .actualReps(exec.getActualReps())
                .actualWeight(exec.getActualWeight())
                .completed(exec.getCompleted())
                .notes(exec.getNotes())
                .build();
    }
}


