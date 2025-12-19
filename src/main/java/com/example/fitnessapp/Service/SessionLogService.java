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
public class SessionLogService { // Service, der Trainingseinheiten startet, updatet und abschließt

    private final SessionLogRepository sessionLogRepository;// Speichert/liest SessionLogs aus der DB
    private final ExecutionLogRepository executionLogRepository; // Speichert/liest ExecutionLogs (pro Übung) aus der DB
    private final ExerciseExecutionTemplateRepository templateRepository; // Holt die Übungs-Templates für eine Session-Vorlage
    private final TrainingSessionRepository1 trainingSessionRepository;  // Holt die Session-Vorlage (Template) aus der DB.


    public SessionLogService(SessionLogRepository sessionLogRepository,// Konstruktor: gibt die Abhängigkeiten rein
                             ExecutionLogRepository executionLogRepository, // Repo für ExecutionLogs
                             ExerciseExecutionTemplateRepository templateRepository,// Repo für Exercise-Templates
                             TrainingSessionRepository1 trainingSessionRepository) {// Repo für Session-Vorlagen
        this.sessionLogRepository = sessionLogRepository;//Speichert das Repo im Feld, damit Methoden es nutzen können
        this.executionLogRepository = executionLogRepository;// Speichert das Repo im Feld
        this.templateRepository = templateRepository;// Speichert das Repo im Feld
        this.trainingSessionRepository = trainingSessionRepository; // Speichert das Repo im Feld
    }

    public SessionLogDetailResponse start(SessionLogCreateRequest request) {// Startet ein neues Training (Log)
        TrainingSession1 templateSession = trainingSessionRepository.findById(request.getSessionTemplateId())// Lädt die Session-Vorlage per ID
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingSession template not found"));// Wenn nicht gefunden: 404

        List<ExerciseExecutionTemplate> templates =  // Liste der Übungs-Templates die in der Session-Vorlage drin sind
                templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(templateSession.getId());// Lädt Übungen sortiert nach Reihenfolge


        if (templates.isEmpty()) {// Prüft: hat die Session-Vorlage überhaupt Übungen?
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,// Wenn keine Übungen: Client-Fehler
                    "TrainingSession template must contain at least one exercise");// Meldung: mindestens eine Übung ist Pflicht
        }

        SessionLog log = SessionLog.builder() // Erstellt ein neues SessionLog-Objekt (Training-Start).
                .templateSession(templateSession) // Verknüpft: dieses Log gehört zu dieser Session-Vorlage.
                .status(LogStatus.IN_PROGRESS) // Setzt Status: Training läuft gerade.
                .startTime(LocalDateTime.now()) // Setzt Startzeit auf „jetzt“.
                .notes(request.getNotes()) // Speichert Notizen aus dem Request.
                .build(); // Baut das SessionLog.

        log = sessionLogRepository.save(log); // Speichert das SessionLog in der DB (dadurch bekommt es z.B. eine ID).

        for (ExerciseExecutionTemplate t : templates) { // Geht jede Übung aus der Vorlage durch.
            ExecutionLog exec = ExecutionLog.builder() // Baut ein ExecutionLog (Log pro Übung in dieser Session).
                    .sessionLog(log) // Verknüpft: gehört zu dem SessionLog.
                    .exerciseTemplate(t) // Verknüpft: basiert auf dem Übungs-Template.
                    .actualSets(t.getPlannedSets()) // Startwert: actualSets = plannedSets (kann später geändert werden).
                    .actualReps(t.getPlannedReps()) // Startwert: actualReps = plannedReps (kann später geändert werden).
                    .actualWeight(t.getPlannedWeight()) // Startwert: actualWeight = plannedWeight (kann später geändert werden).
                    .completed(false) // Anfangs ist die Übung noch nicht als „fertig“ markiert.
                    .notes(null) // Anfangs sind keine Notizen zur Übung gesetzt.
                    .build(); // Baut das ExecutionLog.
            executionLogRepository.save(exec); // Speichert das ExecutionLog in der DB.
        }
        // Nach dem Anlegen der Exercise-Logs erneut laden, damit die Beziehung gefüllt ist
        SessionLog reloaded = sessionLogRepository.findById(log.getId())// Lädt das SessionLog nochmal (inkl. Beziehungen).
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Created log not found"));

        return toDetail(reloaded);// Wandelt das SessionLog in ein Detail-Response-DTO um und gibt es zurück
    }

    public SessionLogDetailResponse getDetail(Long id) {// Gibt ein vollständiges Detail-DTO für ein SessionLog zurück.
        SessionLog log = sessionLogRepository.findById(id) // Sucht das SessionLog in der DB
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));
        return toDetail(log); // Konvertiert Entity zu Detail-DTO und gibt es zurück.
    }

    public ExecutionLogResponse updateExecution(ExecutionLogUpdateRequest request) {// Aktualisiert die echten Werte einer Übung während des Trainings
        ExecutionLog exec = executionLogRepository.findById(request.getExecutionLogId())// Lädt das ExecutionLog per ID.
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ExecutionLog not found"));

        SessionLog log = exec.getSessionLog();// Holt das SessionLog, zu dem dieses ExecutionLog gehört.
        if (log.getStatus() != LogStatus.IN_PROGRESS) {// Prüft: darf man noch ändern?
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Changes only allowed while training is IN_PROGRESS");
        }

        if (request.getActualSets() == null || request.getActualSets() <= 0 ||//Prüft: Sets müssen vorhanden und > 0 sein.
                request.getActualReps() == null || request.getActualReps() <= 0 ||// Prüft: Reps müssen vorhanden und > 0 sein.
                request.getActualWeight() == null || request.getActualWeight() < 0) {// Prüft: Gewicht muss vorhanden und >= 0 sein.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid actual values");
        }
        exec.setActualSets(request.getActualSets()); // Speichert die neuen Sets.
        exec.setActualReps(request.getActualReps()); // Speichert die neuen Reps.
        exec.setActualWeight(request.getActualWeight()); // Speichert das neue Gewicht.
        exec.setCompleted(request.getCompleted() != null ? request.getCompleted() : exec.getCompleted()); // Setzt completed nur, wenn im Request gesetzt; sonst bleibt alter Wert.
        exec.setNotes(request.getNotes()); // Setzt Notizen (kann auch null sein und überschreibt dann).

        return toExecutionResponse(executionLogRepository.save(exec)); // Speichert die Änderungen und gibt das Response-DTO zurück.
    }

    public SessionLogSummaryResponse complete(Long logId) {// Markiert ein Training als abgeschlossen
        SessionLog log = sessionLogRepository.findById(logId)// Lädt das SessionLog.
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));

        if (log.getStatus() != LogStatus.IN_PROGRESS) {// Prüft: nur laufende Trainings darf man abschließen.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only IN_PROGRESS logs can be completed");
        }

        log.setStatus(LogStatus.COMPLETED);// Setzt Status auf COMPLETED.
        log.setEndTime(LocalDateTime.now());//Setzt Endzeit auf „jetzt“.

        return toSummary(sessionLogRepository.save(log));// Speichert das Log und gibt eine Summary-Antwort zurück.
    }

    public void abort(Long logId) {// Bricht ein laufendes Training ab und löscht das Log.
        SessionLog log = sessionLogRepository.findById(logId) // Lädt das SessionLog.
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SessionLog not found"));

        if (log.getStatus() != LogStatus.IN_PROGRESS) {// Prüft: nur laufende Trainings darf man löschen/abbrechen.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only IN_PROGRESS logs can be deleted");
        }

        sessionLogRepository.delete(log);// Löscht das SessionLog (und ggf. abhängige Logs, je nach DB-Relation).
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

    private SessionLogDetailResponse toDetail(SessionLog log) {// Hilfsmethode: macht aus SessionLog ein Summary-DTO.
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

    private ExecutionLogResponse toExecutionResponse(ExecutionLog exec) { // Hilfsmethode: macht aus ExecutionLog ein Response-DTO.
        return ExecutionLogResponse.builder() // Startet Builder fürs Execution-DTO.
                .id(exec.getId()) // Setzt ExecutionLog-ID.
                .exerciseTemplateId(exec.getExerciseTemplate().getId()) // Setzt ID des Übungs-Templates.
                .exerciseName(exec.getExerciseTemplate().getExercise().getName()) // Setzt Übungsname (aus verknüpfter Übung).
                .plannedSets(exec.getExerciseTemplate().getPlannedSets()) // Setzt geplante Sets (aus Template).
                .plannedReps(exec.getExerciseTemplate().getPlannedReps()) // Setzt geplante Reps (aus Template).
                .plannedWeight(exec.getExerciseTemplate().getPlannedWeight()) // Setzt geplantes Gewicht (aus Template).
                .actualSets(exec.getActualSets()) // Setzt echte Sets (aus Log).
                .actualReps(exec.getActualReps()) // Setzt echte Reps (aus Log).
                .actualWeight(exec.getActualWeight()) // Setzt echtes Gewicht (aus Log).
                .completed(exec.getCompleted()) // Setzt, ob Übung abgeschlossen ist.
                .notes(exec.getNotes()) // Setzt Notizen zur Übung.
                .build(); // Baut das DTO.
    }
}


