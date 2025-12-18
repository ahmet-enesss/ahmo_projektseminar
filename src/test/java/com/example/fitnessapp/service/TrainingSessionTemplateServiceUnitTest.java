package com.example.fitnessapp.service;

import com.example.fitnessapp.DTOs.TrainingSessionTemplateRequest;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.SessionLogRepository;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import com.example.fitnessapp.Service.TrainingSessionTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionTemplateServiceUnitTest {

    @Mock
    private TrainingSessionRepository1 sessionRepository;
    @Mock
    private TrainingPlanRepository1 planRepository;
    @Mock
    private ExerciseExecutionTemplateRepository exerciseTemplateRepository;
    @Mock
    private SessionLogRepository sessionLogRepository;

    @InjectMocks
    private TrainingSessionTemplateService service;

    private TrainingSession1 session;

    @BeforeEach
    void setUp() {
        session = TrainingSession1.builder().id(2L).name("S").orderIndex(1).build();
    }

    @Test
    void createSession_whenNameMissing_shouldThrow() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("");
        req.setOrderIndex(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createSession(req));
        assertTrue(ex.getMessage().contains("Name ist erforderlich"));
    }

    @Test
    void createSession_whenOrderInvalid_shouldThrow() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("X");
        req.setOrderIndex(0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createSession(req));
        assertTrue(ex.getMessage().contains("Reihenfolge muss zwischen 1 und 30 liegen"));
    }

    @Test
    void getSessionById_whenNotFound_shouldThrow() {
        when(sessionRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getSessionById(5L));
        assertTrue(ex.getMessage().contains("Session-Vorlage nicht gefunden"));
    }

    @Test
    void createSession_whenPlanExistsAndOrderTaken_shouldThrow() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("X");
        req.setOrderIndex(1);
        req.setPlanId(4L);

        when(planRepository.findById(4L)).thenReturn(Optional.of(TrainingPlan1.builder().id(4L).name("P").build()));
        when(sessionRepository.countByTrainingPlan_Id(4L)).thenReturn(1L);
        when(sessionRepository.findByTrainingPlan_IdAndOrderIndex(4L, 1)).thenReturn(Optional.of(session));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createSession(req));
        assertTrue(ex.getMessage().contains("bereits für diesen Plan vergeben"));
    }

    @Test
    void createSession_happyPath_savesAndReturns() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("X");
        req.setOrderIndex(2);
        req.setPlanId(null);

        when(sessionRepository.save(any(TrainingSession1.class))).thenAnswer(i -> {
            TrainingSession1 s = i.getArgument(0);
            s.setId(11L);
            return s;
        });

        var resp = service.createSession(req);

        assertEquals(11L, resp.getId());
        verify(sessionRepository).save(any());
    }

    @Test
    void update_whenSessionNotFound_shouldThrow() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("X");
        req.setOrderIndex(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateSession(99L, req));
        assertTrue(ex.getMessage().contains("Session-Vorlage nicht gefunden"));
    }

    @Test
    void update_whenValid_savesAndReturns() {
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(sessionRepository.findByTrainingPlan_IdAndOrderIndexAndIdNot(4L, 2, 2L)).thenReturn(Optional.empty());
        when(planRepository.findById(4L)).thenReturn(Optional.of(TrainingPlan1.builder().id(4L).name("P").build()));
        when(sessionRepository.save(any(TrainingSession1.class))).thenAnswer(i -> i.getArgument(0));

        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("New");
        req.setOrderIndex(2);
        req.setPlanId(4L);

        var resp = service.updateSession(2L, req);
        assertEquals("New", resp.getName());
    }

    @Test
    void create_whenPlanHasMaxSessions_throws() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("X");
        req.setOrderIndex(1);
        req.setPlanId(4L);

        when(planRepository.findById(4L)).thenReturn(Optional.of(TrainingPlan1.builder().id(4L).build()));
        when(sessionRepository.countByTrainingPlan_Id(4L)).thenReturn(30L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createSession(req));
        assertTrue(ex.getMessage().contains("maximal 30 Sessions"));
    }

    @Test
    void delete_whenNotFound_shouldThrow() {
        when(sessionRepository.findById(77L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.deleteSession(77L));
        assertTrue(ex.getMessage().contains("Session-Vorlage nicht gefunden"));
    }
}
