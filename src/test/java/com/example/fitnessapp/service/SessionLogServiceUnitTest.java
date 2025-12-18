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

@ExtendWith(MockitoExtension.class)
class SessionLogServiceUnitTest {

    @Mock
    private SessionLogRepository sessionLogRepository;
    @Mock
    private ExecutionLogRepository executionLogRepository;
    @Mock
    private ExerciseExecutionTemplateRepository templateRepository;
    @Mock
    private TrainingSessionRepository1 trainingSessionRepository;

    @InjectMocks
    private SessionLogService service;

    private TrainingSession1 templateSession;

    @BeforeEach
    void setUp() {
        templateSession = TrainingSession1.builder().id(10L).name("Temp").build();
    }

    @Test
    void start_whenNoTemplates_shouldThrow() {
        when(trainingSessionRepository.findById(10L)).thenReturn(Optional.of(templateSession));
        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(10L)).thenReturn(List.of());

        SessionLogCreateRequest req = new SessionLogCreateRequest();
        req.setSessionTemplateId(10L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.start(req));
        assertTrue(ex.getMessage().contains("must contain at least one exercise"));
    }

    @Test
    void start_happyPath_createsLogs() {
        ExerciseExecutionTemplate t = ExerciseExecutionTemplate.builder()
                .id(5L)
                .plannedSets(3)
                .plannedReps(10)
                .plannedWeight(0.0)
                .build();

        when(trainingSessionRepository.findById(10L)).thenReturn(Optional.of(templateSession));
        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(10L)).thenReturn(List.of(t));
        when(sessionLogRepository.save(any(SessionLog.class))).thenAnswer(i -> {
            SessionLog s = i.getArgument(0);
            s.setId(99L);
            return s;
        });
        when(sessionLogRepository.findById(99L)).thenReturn(Optional.of(SessionLog.builder().id(99L).templateSession(templateSession).startTime(java.time.LocalDateTime.now()).status(LogStatus.IN_PROGRESS).build()));

        SessionLogCreateRequest req = new SessionLogCreateRequest();
        req.setSessionTemplateId(10L);

        var detail = service.start(req);

        assertNotNull(detail);
        assertEquals(99L, detail.getId());
        verify(executionLogRepository, times(1)).save(any());
    }

    @Test
    void updateExecution_whenInvalidValues_shouldThrow() {
        ExecutionLog exec = ExecutionLog.builder().id(7L).actualSets(3).actualReps(10).actualWeight(0.0).completed(false).sessionLog(SessionLog.builder().id(1L).status(LogStatus.IN_PROGRESS).build()).exerciseTemplate(ExerciseExecutionTemplate.builder().id(5L).build()).build();
        when(executionLogRepository.findById(7L)).thenReturn(Optional.of(exec));

        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(7L);
        req.setActualSets(0); // invalid
        req.setActualReps(0);
        req.setActualWeight(-1.0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateExecution(req));
        assertTrue(ex.getMessage().contains("Invalid actual values"));
    }

    @Test
    void updateExecution_whenNotInProgress_shouldThrow() {
        ExecutionLog exec = ExecutionLog.builder().id(7L).actualSets(3).actualReps(10).actualWeight(0.0).completed(false).sessionLog(SessionLog.builder().id(1L).status(LogStatus.COMPLETED).build()).exerciseTemplate(ExerciseExecutionTemplate.builder().id(5L).build()).build();
        when(executionLogRepository.findById(7L)).thenReturn(Optional.of(exec));

        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(7L);
        req.setActualSets(3);
        req.setActualReps(10);
        req.setActualWeight(0.0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateExecution(req));
        assertTrue(ex.getMessage().contains("Changes only allowed while training is IN_PROGRESS"));
    }

    @Test
    void updateExecution_whenValid_updatesAndReturns() {
        ExecutionLog exec = ExecutionLog.builder().id(7L).actualSets(3).actualReps(10).actualWeight(0.0).completed(false).sessionLog(SessionLog.builder().id(1L).status(LogStatus.IN_PROGRESS).build()).exerciseTemplate(ExerciseExecutionTemplate.builder().id(5L).plannedSets(3).plannedReps(10).plannedWeight(0.0).exercise(Exercise1.builder().id(2L).name("E").build()).build()).build();
        when(executionLogRepository.findById(7L)).thenReturn(Optional.of(exec));
        when(executionLogRepository.save(any(ExecutionLog.class))).thenAnswer(i -> i.getArgument(0));

        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(7L);
        req.setActualSets(4);
        req.setActualReps(12);
        req.setActualWeight(10.0);
        req.setCompleted(true);
        req.setNotes("good");

        var resp = service.updateExecution(req);

        assertEquals(4, resp.getActualSets());
        assertEquals(12, resp.getActualReps());
        assertEquals(10.0, resp.getActualWeight());
        assertTrue(resp.getCompleted());
        assertEquals("good", resp.getNotes());
    }

    @Test
    void updateExecution_whenNotFound_shouldThrow() {
        when(executionLogRepository.findById(123L)).thenReturn(Optional.empty());

        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(123L);
        req.setActualSets(1);
        req.setActualReps(1);
        req.setActualWeight(1.0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateExecution(req));
        assertTrue(ex.getMessage().contains("ExecutionLog not found"));
    }

    @Test
    void updateExecution_whenCompletedNull_keepsPreviousCompletedValue() {
        ExecutionLog exec = ExecutionLog.builder().id(8L).actualSets(3).actualReps(10).actualWeight(0.0).completed(false).sessionLog(SessionLog.builder().id(2L).status(LogStatus.IN_PROGRESS).build()).exerciseTemplate(ExerciseExecutionTemplate.builder().id(5L).plannedSets(3).plannedReps(10).plannedWeight(0.0).exercise(Exercise1.builder().id(2L).name("E").build()).build()).build();
        when(executionLogRepository.findById(8L)).thenReturn(Optional.of(exec));
        when(executionLogRepository.save(any(ExecutionLog.class))).thenAnswer(i -> i.getArgument(0));

        ExecutionLogUpdateRequest req = new ExecutionLogUpdateRequest();
        req.setExecutionLogId(8L);
        req.setActualSets(4);
        req.setActualReps(11);
        req.setActualWeight(5.0);
        req.setCompleted(null);

        var resp = service.updateExecution(req);

        assertFalse(resp.getCompleted());
    }

    @Test
    void complete_whenNotInProgress_shouldThrow() {
        when(sessionLogRepository.findById(5L)).thenReturn(Optional.of(SessionLog.builder().id(5L).status(LogStatus.COMPLETED).build()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.complete(5L));
        assertTrue(ex.getMessage().contains("Only IN_PROGRESS logs can be completed"));
    }

    @Test
    void complete_whenInProgress_completesAndReturnsSummary() {
        SessionLog log = SessionLog.builder().id(6L).status(LogStatus.IN_PROGRESS).templateSession(templateSession).startTime(java.time.LocalDateTime.now()).build();
        when(sessionLogRepository.findById(6L)).thenReturn(Optional.of(log));
        when(sessionLogRepository.save(any(SessionLog.class))).thenAnswer(i -> i.getArgument(0));

        var summary = service.complete(6L);

        assertEquals(6L, summary.getId());
        assertEquals(LogStatus.COMPLETED, summary.getStatus());
    }

    @Test
    void abort_whenNotInProgress_shouldThrow() {
        when(sessionLogRepository.findById(8L)).thenReturn(Optional.of(SessionLog.builder().id(8L).status(LogStatus.COMPLETED).build()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.abort(8L));
        assertTrue(ex.getMessage().contains("Only IN_PROGRESS logs can be deleted"));
    }

    @Test
    void abort_whenInProgress_deletes() {
        SessionLog log = SessionLog.builder().id(9L).status(LogStatus.IN_PROGRESS).build();
        when(sessionLogRepository.findById(9L)).thenReturn(Optional.of(log));

        service.abort(9L);

        verify(sessionLogRepository).delete(log);
    }

    @Test
    void getDetail_withExecutions_returnsDetailWithExecutionInfo() {
        TrainingSession1 template = TrainingSession1.builder().id(20L).name("TempName").build();
        ExecutionLog exec = ExecutionLog.builder()
                .id(30L)
                .actualSets(2)
                .actualReps(5)
                .actualWeight(12.5)
                .completed(true)
                .notes("ok")
                .exerciseTemplate(ExerciseExecutionTemplate.builder()
                        .id(40L)
                        .plannedSets(3)
                        .plannedReps(8)
                        .plannedWeight(10.0)
                        .exercise(Exercise1.builder().id(50L).name("ExName").category("Cat").build())
                        .build())
                .sessionLog(SessionLog.builder().id(21L).templateSession(template).status(LogStatus.IN_PROGRESS).build())
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
}
