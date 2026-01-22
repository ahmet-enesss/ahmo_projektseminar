package com.example.fitnessapp.Service;

import com.example.fitnessapp.DTOs.*;
import com.example.fitnessapp.Model.*;
import com.example.fitnessapp.Repository.ExecutionLogRepository;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.SessionLogRepository;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import com.example.fitnessapp.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;

    public SessionLogService(SessionLogRepository sessionLogRepository,
                             ExecutionLogRepository executionLogRepository,
                             ExerciseExecutionTemplateRepository templateRepository,
                             TrainingSessionRepository1 trainingSessionRepository,
                             UserRepository userRepository) {
        this.sessionLogRepository = sessionLogRepository;
        this.executionLogRepository = executionLogRepository;
        this.templateRepository = templateRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Hilfsmethode zum Abrufen des aktuell angemeldeten Benutzers aus dem SecurityContext
     * @return Der aktuelle User
     * @throws ResponseStatusException wenn der Benutzer nicht authentifiziert ist
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal() == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Benutzer nicht authentifiziert");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Benutzer nicht gefunden"));
    }

    public SessionLogDetailResponse start(SessionLogCreateRequest request) {
        // Hole aktuellen Benutzer (automatische Zuweisung)
        User currentUser = getCurrentUser();
        
        TrainingSession1 templateSession = trainingSessionRepository.findById(request.getSessionTemplateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingSession template not found"));

        List<ExerciseExecutionTemplate> templates =
                templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(templateSession.getId());

        if (templates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "TrainingSession template must contain at least one exercise");
        }

        SessionLog log = SessionLog.builder()
                .templateSession(templateSession)
                .user(currentUser) // Automatische Zuweisung zum angemeldeten Benutzer
                .status(LogStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        log = sessionLogRepository.save(log);

        for (ExerciseExecutionTemplate t : templates) {
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

    public SessionLogDetailResponse getDetail(Long id) {
        User currentUser = getCurrentUser();
        // Nur SessionLogs des aktuellen Benutzers können abgerufen werden (User-Isolation)
        SessionLog log = sessionLogRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));
        return toDetail(log);
    }

    public ExecutionLogResponse updateExecution(ExecutionLogUpdateRequest request) {
        ExecutionLog exec;
        SessionLog log;

        // Prüfe ob ExecutionLog existiert (negative IDs oder nicht vorhandene IDs bedeuten: neu erstellen)
        if (request.getExecutionLogId() == null || request.getExecutionLogId() <= 0 || 
            !executionLogRepository.existsById(request.getExecutionLogId())) {
            // Neuen ExecutionLog erstellen
            if (request.getSessionLogId() == null || request.getExerciseTemplateId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "sessionLogId and exerciseTemplateId are required when creating a new ExecutionLog");
            }

            User currentUser = getCurrentUser();
            // Nur SessionLogs des aktuellen Benutzers können bearbeitet werden (User-Isolation)
            log = sessionLogRepository.findByIdAndUser(request.getSessionLogId(), currentUser)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));

            if (log.getStatus() != LogStatus.IN_PROGRESS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Changes only allowed while training is IN_PROGRESS");
            }

            ExerciseExecutionTemplate template = templateRepository.findById(request.getExerciseTemplateId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ExerciseExecutionTemplate not found"));

            // Validiere Ist-Werte
            if (request.getActualSets() == null || request.getActualSets() <= 0 ||
                    request.getActualReps() == null || request.getActualReps() <= 0 ||
                    request.getActualWeight() == null || request.getActualWeight() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid actual values");
            }

            // Prüfe ob bereits ein ExecutionLog für diese Kombination existiert
            exec = executionLogRepository.findBySessionLogAndExerciseTemplate(log, template)
                    .orElse(null);

            if (exec == null) {
                // Erstelle neuen ExecutionLog
                exec = ExecutionLog.builder()
                        .sessionLog(log)
                        .exerciseTemplate(template)
                        .actualSets(request.getActualSets())
                        .actualReps(request.getActualReps())
                        .actualWeight(request.getActualWeight())
                        .completed(request.getCompleted() != null ? request.getCompleted() : false)
                        .notes(request.getNotes())
                        .build();
            } else {
                // Aktualisiere existierenden ExecutionLog
                exec.setActualSets(request.getActualSets());
                exec.setActualReps(request.getActualReps());
                exec.setActualWeight(request.getActualWeight());
                // Status immer speichern: wenn completed explizit gesetzt ist, verwende den Wert, sonst false
                exec.setCompleted(request.getCompleted() != null ? request.getCompleted() : false);
                if (request.getNotes() != null) {
                    exec.setNotes(request.getNotes());
                }
            }
        } else {
            // Bestehenden ExecutionLog aktualisieren
            exec = executionLogRepository.findById(request.getExecutionLogId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ExecutionLog not found"));

            log = exec.getSessionLog();
            
            // Prüfe User-Isolation: Nur der Besitzer kann ExecutionLogs bearbeiten
            User currentUser = getCurrentUser();
            if (!log.getUser().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Zugriff verweigert: SessionLog gehört nicht dem aktuellen Benutzer");
            }
            
            if (log.getStatus() != LogStatus.IN_PROGRESS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Changes only allowed while training is IN_PROGRESS");
            }

            if (request.getActualSets() == null || request.getActualSets() <= 0 ||
                    request.getActualReps() == null || request.getActualReps() <= 0 ||
                    request.getActualWeight() == null || request.getActualWeight() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid actual values");
            }

            exec.setActualSets(request.getActualSets());
            exec.setActualReps(request.getActualReps());
            exec.setActualWeight(request.getActualWeight());
            // Status immer speichern: wenn completed explizit gesetzt ist, verwende den Wert, sonst false
            exec.setCompleted(request.getCompleted() != null ? request.getCompleted() : false);
            exec.setNotes(request.getNotes());
        }

        return toExecutionResponse(executionLogRepository.save(exec));
    }

    public SessionLogSummaryResponse complete(Long logId) {
        User currentUser = getCurrentUser();
        // Nur SessionLogs des aktuellen Benutzers können abgeschlossen werden (User-Isolation)
        SessionLog log = sessionLogRepository.findByIdAndUser(logId, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));

        if (log.getStatus() != LogStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only IN_PROGRESS logs can be completed");
        }

        log.setStatus(LogStatus.COMPLETED);
        log.setEndTime(LocalDateTime.now());

        return toSummary(sessionLogRepository.save(log));
    }

    public void abort(Long logId) {
        User currentUser = getCurrentUser();
        // Nur SessionLogs des aktuellen Benutzers können abgebrochen werden (User-Isolation)
        SessionLog log = sessionLogRepository.findByIdAndUser(logId, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));

        if (log.getStatus() != LogStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only IN_PROGRESS logs can be deleted");
        }

        sessionLogRepository.delete(log);
    }

    /**
     * Gibt die Trainingshistorie des aktuell angemeldeten Benutzers zurück
     * @return Liste aller SessionLogs des Benutzers, sortiert nach Startzeit (neueste zuerst)
     */
    public List<SessionLogSummaryResponse> getTrainingHistory() {
        User currentUser = getCurrentUser();
        List<SessionLog> logs = sessionLogRepository.findByUserOrderByStartTimeDesc(currentUser);
        return logs.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    private SessionLogSummaryResponse toSummary(SessionLog log) {
        return SessionLogSummaryResponse.builder()
                .id(log.getId())
                .sessionTemplateId(log.getTemplateSession().getId())
                .sessionName(log.getTemplateSession().getName())
                .startTime(log.getStartTime())
                .endTime(log.getEndTime())
                .status(log.getStatus())
                .build();
    }

    private SessionLogDetailResponse toDetail(SessionLog log) {
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

    private ExecutionLogResponse toExecutionResponse(ExecutionLog exec) {
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


