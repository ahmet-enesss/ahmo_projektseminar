package com.example.fitnessapp.service;

import com.example.fitnessapp.DTOs.ExecutionLogUpdateRequest;
import com.example.fitnessapp.DTOs.SessionLogCreateRequest;
import com.example.fitnessapp.Model.*;
import com.example.fitnessapp.Repository.ExecutionLogRepository;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.SessionLogRepository;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import com.example.fitnessapp.Service.SessionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Aktiviert Mockito-Unterstützung für JUnit 5
// Ermöglicht das Mocken von Repositories und das automatische Injizieren in den Service
@ExtendWith(MockitoExtension.class)
class SessionLogServiceUnitTest {

    @Mock// Mock für SessionLogRepository (Session-Logs in der DB)
    private SessionLogRepository sessionLogRepository;

    @Mock// Mock für ExecutionLogRepository (Ausführungs-Logs pro Übung)
    private ExecutionLogRepository executionLogRepository;

    @Mock// Mock für Templates der Übungen innerhalb einer Session
    private ExerciseExecutionTemplateRepository templateRepository;

    @Mock// Mock für Trainingssession-Vorlagen
    private TrainingSessionRepository1 trainingSessionRepository;

    @InjectMocks// Das zu testende Service-Objekt
    // Mockito injiziert alle oben definierten Mocks automatisch
    private SessionLogService service;

    private TrainingSession1 templateSession;

    // Wird vor jedem Test ausgeführt
    // Erstellt eine gültige Trainingssession-Vorlage für mehrere Tests
    @BeforeEach
    void setUp() {
        templateSession = TrainingSession1.builder().id(10L).name("Temp").build();
    }

    // Testfall:
    // Eine Trainingssession existiert enthält aber KEINE Übungstemplates
    // --> Laut Business-Regel darf eine Session ohne Übungen nicht gestartet werden
    @Test
    void start_whenNoTemplates_shouldThrow() {
        when(trainingSessionRepository.findById(10L)).thenReturn(Optional.of(templateSession));
        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of());
        SessionLogCreateRequest req = new SessionLogCreateRequest();
        req.setSessionTemplateId(10L);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.start(req)
        );
        // Erwartung: Fehlermeldung weist darauf hin, dass mindestens eine Übung nötig ist
        assertTrue(ex.getMessage().contains("must contain at least one exercise"));
    }

    // Testfall (Happy Path):
    // Eine gültige Session mit mindestens einem Template wird gestartet
    // -->  Es werden ein SessionLog und dazugehörige ExecutionLogs erzeugt
    @Test
    void start_happyPath_createsLogs() {
        ExerciseExecutionTemplate t = ExerciseExecutionTemplate.builder()
                .id(5L)
                .plannedSets(3)
                .plannedReps(10)
                .plannedWeight(0.0)
                .build();
        when(trainingSessionRepository.findById(10L)).thenReturn(Optional.of(templateSession));
        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of(t));
        // Simuliert das Speichern des SessionLogs inkl. ID-Generierung
        when(sessionLogRepository.save(any(SessionLog.class))).thenAnswer(i -> {
            SessionLog s = i.getArgument(0);
            s.setId(99L);
            return s;
        });
        // Service lädt das gespeicherte Log erneut
        when(sessionLogRepository.findById(99L)).thenReturn(
                Optional.of(SessionLog.builder()
                        .id(99L)
                        .templateSession(templateSession)
                        .startTime(java.time.LocalDateTime.now())
                        .status(LogStatus.IN_PROGRESS)
                        .build())
        );
        SessionLogCreateRequest req = new SessionLogCreateRequest();
        req.setSessionTemplateId(10L);
        var detail = service.start(req);
        // Überprüfung der Rückgabe
        assertNotNull(detail);
        assertEquals(99L, detail.getId());
        // Sicherstellen dass ExecutionLogs erstellt wurden
        verify(executionLogRepository, times(1)).save(any());
    }

    // Testfall:
    // Ungültige tatsächliche Werte (Sets <= 0, Reps <= 0, Gewicht < 0)
    // --> müssen durch die Validierung abgelehnt werden
    @Test
    void updateExecution_whenInvalidValues_shouldThrow() {
        ExecutionLog exec = ExecutionLog.builder()
                .id(7L)
                .actualSets(3)
                .actualReps(10)
                .actualWeight(0.0)
                .completed(false)
                .sessionLog(SessionLog.builder().id(1L).status(LogStatus.IN_PROGRESS).build())
                .exerciseTemplate(ExerciseExecutionTemplate.builder().id(5L).build())
                .build();
        when(executionLogRepository.findById(7L)).thenReturn(Optional.of(exec));

        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(7L);
        req.setActualSets(0);   // ungültig
        req.setActualReps(0);   // ungültig
        req.setActualWeight(-1.0); // ungültig
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.updateExecution(req)
        );
        assertTrue(ex.getMessage().contains("Invalid actual values"));
    }

    // Testfall:
    // Änderungen sind nur erlaubt wenn die Session IN_PROGRESS ist
    @Test
    void updateExecution_whenNotInProgress_shouldThrow() {
        ExecutionLog exec = ExecutionLog.builder()
                .id(7L)
                .sessionLog(SessionLog.builder().id(1L).status(LogStatus.COMPLETED).build())
                .exerciseTemplate(ExerciseExecutionTemplate.builder().id(5L).build())
                .build();
        when(executionLogRepository.findById(7L)).thenReturn(Optional.of(exec));
        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(7L);
        req.setActualSets(3);
        req.setActualReps(10);
        req.setActualWeight(0.0);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.updateExecution(req)
        );
        assertTrue(ex.getMessage().contains("IN_PROGRESS"));
    }

    // Testfall (Happy Path):
    // Gültige Werte, Session ist IN_PROGRESS → Update erfolgreich
    @Test
    void updateExecution_whenValid_updatesAndReturns() {
        ExecutionLog exec = ExecutionLog.builder()
                .id(7L)
                .sessionLog(SessionLog.builder().id(1L).status(LogStatus.IN_PROGRESS).build())
                .exerciseTemplate(ExerciseExecutionTemplate.builder()
                        .plannedSets(3)
                        .plannedReps(10)
                        .plannedWeight(0.0)
                        .exercise(Exercise1.builder().id(2L).name("E").build())
                        .build())
                .completed(false)
                .build();
        when(executionLogRepository.findById(7L)).thenReturn(Optional.of(exec));
        when(executionLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(7L);
        req.setActualSets(4);
        req.setActualReps(12);
        req.setActualWeight(10.0);
        req.setCompleted(true);
        req.setNotes("good");
        var resp = service.updateExecution(req);
        // Überprüfung aller aktualisierten Felder
        assertEquals(4, resp.getActualSets());
        assertEquals(12, resp.getActualReps());
        assertEquals(10.0, resp.getActualWeight());
        assertTrue(resp.getCompleted());
        assertEquals("good", resp.getNotes());
    }

    // Testfall:
    // ExecutionLog mit der ID existiert nicht → 404 Fehler
    @Test
    void updateExecution_whenNotFound_shouldThrow() {
        when(executionLogRepository.findById(123L)).thenReturn(Optional.empty());
        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(123L);
        req.setActualSets(1);
        req.setActualReps(1);
        req.setActualWeight(1.0);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.updateExecution(req)
        );
        assertTrue(ex.getMessage().contains("ExecutionLog not found"));
    }

    // Testfall:
    // completed = null → bestehender Wert darf NICHT überschrieben werden.
    @Test
    void updateExecution_whenCompletedNull_keepsPreviousCompletedValue() {
        ExecutionLog exec = ExecutionLog.builder()
                .id(8L)
                .completed(false)
                .sessionLog(SessionLog.builder().status(LogStatus.IN_PROGRESS).build())
                .exerciseTemplate(ExerciseExecutionTemplate.builder()
                        .exercise(Exercise1.builder().id(2L).build())
                        .build())
                .build();
        when(executionLogRepository.findById(8L)).thenReturn(Optional.of(exec));
        when(executionLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(8L);
        req.setActualSets(4);
        req.setActualReps(11);
        req.setActualWeight(5.0);
        req.setCompleted(null);
        var resp = service.updateExecution(req);
        assertFalse(resp.getCompleted());
    }

    // Testfall:
    // Eine Session die nicht IN_PROGRESS ist, darf nicht abgeschlossen werden
    @Test
    void complete_whenNotInProgress_shouldThrow() {
        when(sessionLogRepository.findById(5L))
                .thenReturn(Optional.of(SessionLog.builder()
                        .id(5L)
                        .status(LogStatus.COMPLETED)
                        .build()));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.complete(5L)
        );
        assertTrue(ex.getMessage().contains("IN_PROGRESS"));
    }

    // Testfall (Happy Path):
    // IN_PROGRESS → Status wird auf COMPLETED gesetzt
    @Test
    void complete_whenInProgress_completesAndReturnsSummary() {
        SessionLog log = SessionLog.builder()
                .id(6L)
                .status(LogStatus.IN_PROGRESS)
                .templateSession(templateSession)
                .startTime(java.time.LocalDateTime.now())
                .build();
        when(sessionLogRepository.findById(6L)).thenReturn(Optional.of(log));
        when(sessionLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        var summary = service.complete(6L);
        assertEquals(6L, summary.getId());
        assertEquals(LogStatus.COMPLETED, summary.getStatus());
    }

    // Testfall:
    // Nur IN_PROGRESS Sessions dürfen abgebrochen werden
    @Test
    void abort_whenNotInProgress_shouldThrow() {
        when(sessionLogRepository.findById(8L))
                .thenReturn(Optional.of(SessionLog.builder()
                        .id(8L)
                        .status(LogStatus.COMPLETED)
                        .build()));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.abort(8L)
        );

        assertTrue(ex.getMessage().contains("IN_PROGRESS"));
    }

    // Testfall (Happy Path):
    // IN_PROGRESS → SessionLog wird gelöscht
    @Test
    void abort_whenInProgress_deletes() {
        SessionLog log = SessionLog.builder()
                .id(9L)
                .status(LogStatus.IN_PROGRESS)
                .build();
        when(sessionLogRepository.findById(9L)).thenReturn(Optional.of(log));
        service.abort(9L);
        verify(sessionLogRepository).delete(log);
    }

    // Testfall:
    // Detail-Ansicht einer Session inklusive ExecutionLogs
    @Test
    void getDetail_withExecutions_returnsDetailWithExecutionInfo() {
        TrainingSession1 template = TrainingSession1.builder()
                .id(20L)
                .name("TempName")
                .build();
        ExecutionLog exec = ExecutionLog.builder()
                .id(30L)
                .actualSets(2)
                .actualReps(5)
                .actualWeight(12.5)
                .completed(true)
                .notes("ok")
                .exerciseTemplate(ExerciseExecutionTemplate.builder()
                        .plannedSets(3)
                        .plannedReps(8)
                        .plannedWeight(10.0)
                        .exercise(Exercise1.builder().id(50L).name("ExName").category("Cat").build())
                        .build())
                .sessionLog(SessionLog.builder().id(21L).status(LogStatus.IN_PROGRESS).build())
                .build();

        SessionLog log = SessionLog.builder()
                .id(21L)
                .templateSession(template)
                .status(LogStatus.IN_PROGRESS)
                .exerciseLogs(Set.of(exec))
                .build();

        when(sessionLogRepository.findById(21L)).thenReturn(Optional.of(log));

        var detail = service.getDetail(21L);

        assertEquals(21L, detail.getId());
        assertEquals(1, detail.getExecutions().size());

        var e = detail.getExecutions().get(0);
        assertEquals("ExName", e.getExerciseName());
        assertEquals(3, e.getPlannedSets());
        assertEquals(2, e.getActualSets());
    }

    // Testfall:
    // Deckt ALLE Validierungs-Branches in updateExecution ab
    @Test
    void updateExecution_shouldCoverAllInvalidValueBranches() {
        ExecutionLog exec = ExecutionLog.builder()
                .id(1L)
                .sessionLog(SessionLog.builder().status(LogStatus.IN_PROGRESS).build())
                .build();
        when(executionLogRepository.findById(1L)).thenReturn(Optional.of(exec));
        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(1L);
        // Fall 1: actualSets == null
        req.setActualSets(null);
        assertThrows(ResponseStatusException.class, () -> service.updateExecution(req));
        // Fall 2: actualReps <= 0
        req.setActualSets(3);
        req.setActualReps(0);
        assertThrows(ResponseStatusException.class, () -> service.updateExecution(req));
        // Fall 3: actualWeight == null
        req.setActualReps(10);
        req.setActualWeight(null);
        assertThrows(ResponseStatusException.class, () -> service.updateExecution(req));
    }
}
