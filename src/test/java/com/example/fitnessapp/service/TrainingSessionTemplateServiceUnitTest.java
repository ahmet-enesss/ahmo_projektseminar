package com.example.fitnessapp.service;

import com.example.fitnessapp.DTOs.TrainingSessionTemplateRequest;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.SessionLogRepository;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import com.example.fitnessapp.Service.TrainingSessionTemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionTemplateServiceUnitTest {

    @Mock private TrainingSessionRepository1 sessionRepository;
    @Mock private TrainingPlanRepository1 planRepository;
    @Mock private ExerciseExecutionTemplateRepository exerciseTemplateRepository;
    @Mock private SessionLogRepository sessionLogRepository;

    @InjectMocks private TrainingSessionTemplateService service;

    @Test
    void shouldThrowExceptionWhenSessionNameIsMissing() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("");
        assertThrows(ResponseStatusException.class, () -> service.createSession(req));
    }

    @Test
    void shouldThrowExceptionWhenOrderIndexIsInvalid() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Session");
        req.setOrderIndex(31); // Max ist 30
        assertThrows(ResponseStatusException.class, () -> service.createSession(req));
    }

    @Test
    void shouldThrowExceptionWhenPlanHasMaxSessions() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setPlanId(1L);
        req.setName("X");
        req.setOrderIndex(1);

        when(planRepository.findById(1L)).thenReturn(Optional.of(new TrainingPlan1()));
        when(sessionRepository.countByTrainingPlan_Id(1L)).thenReturn(30L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createSession(req));
        assertTrue(ex.getReason().contains("maximal 30 Sessions"));
    }

    @Test
    void shouldThrowConflictExceptionWhenOrderIndexAlreadyUsed() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setPlanId(1L);
        req.setOrderIndex(5);
        req.setName("New");

        when(planRepository.findById(1L)).thenReturn(Optional.of(new TrainingPlan1()));
        when(sessionRepository.countByTrainingPlan_Id(1L)).thenReturn(5L);
        when(sessionRepository.findByTrainingPlan_IdAndOrderIndex(1L, 5)).thenReturn(Optional.of(new TrainingSession1()));

        assertThrows(ResponseStatusException.class, () -> service.createSession(req));
    }

    @Test
    void updateSession_shouldThrowNotFoundWhenIdInvalid() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Test");
        req.setOrderIndex(1);

        assertThrows(ResponseStatusException.class, () -> service.updateSession(99L, req));
    }

    @Test
    void createSession_shouldWorkWithoutPlanId() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Freies Training");
        req.setOrderIndex(1);
        // planId ist null

        when(sessionRepository.save(any())).thenAnswer(i -> {
            TrainingSession1 s = i.getArgument(0);
            s.setId(100L);
            return s;
        });

        var resp = service.createSession(req);
        assertEquals("Freies Training", resp.getName());
        assertNull(resp.getPlanId());
    }

    @Test
    void updateSession_shouldWorkWithoutPlan() {
        TrainingSession1 session = TrainingSession1.builder().id(1L).name("Old").orderIndex(1).build();
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("New Name");
        req.setOrderIndex(2);
        req.setPlanId(null); // Wichtiger Branch: plan == null

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.updateSession(1L, req);

        assertEquals("New Name", result.getName());
        assertNull(result.getPlanId());
    }

    @Test
    void createSession_shouldThrowWhenNameIsNull() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName(null); // Testet if (request.getName() == null)
        assertThrows(ResponseStatusException.class, () -> service.createSession(req));
    }
}