package com.example.fitnessapp.service;

import com.example.fitnessapp.DTOs.TrainingPlanDetailResponse;
import com.example.fitnessapp.DTOs.TrainingPlanRequest;
import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import com.example.fitnessapp.Service.TrainingPlanService1;
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
class TrainingPlanServiceUnitTest {

    @Mock
    private TrainingPlanRepository1 planRepository;
    @Mock
    private TrainingSessionRepository1 sessionRepository;
    @Mock
    private ExerciseExecutionTemplateRepository templateRepository;

    @InjectMocks
    private TrainingPlanService1 service;

    private TrainingPlan1 plan;

    @BeforeEach
    void setUp() {
        plan = TrainingPlan1.builder().id(1L).name("Plan").description("D").build();
    }

    @Test
    void getAllTrainingPlans_returnsOverview() {
        when(planRepository.findAll()).thenReturn(List.of(plan));
        when(sessionRepository.countByTrainingPlan_Id(1L)).thenReturn(2L);

        var list = service.getAllTrainingPlans();

        assertEquals(1, list.size());
        assertEquals(2L, list.get(0).getSessionCount());
    }

    @Test
    void getTrainingPlanById_whenNotFound_throws() {
        when(planRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getTrainingPlanById(5L));
        assertTrue(ex.getMessage().contains("TrainingPlan not found"));
    }

    @Test
    void getTrainingPlanById_returnsDetail_withSessionsHint() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(sessionRepository.findByTrainingPlan_IdOrderByOrderIndexAsc(1L)).thenReturn(List.of());

        TrainingPlanDetailResponse resp = service.getTrainingPlanById(1L);

        assertFalse(resp.isHasSessions());
        assertTrue(resp.getSessionsHint().contains("Noch keine"));
    }

    @Test
    void getTrainingPlanById_withSessions_returnsHasSessionsTrueAndSummaries() {
        TrainingSession1 s = TrainingSession1.builder().id(7L).name("S1").orderIndex(1).build();
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(sessionRepository.findByTrainingPlan_IdOrderByOrderIndexAsc(1L)).thenReturn(List.of(s));
        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(7L)).thenReturn(List.of(ExerciseExecutionTemplate.builder().id(2L).build()));

        TrainingPlanDetailResponse resp = service.getTrainingPlanById(1L);

        assertTrue(resp.isHasSessions());
        assertEquals(1, resp.getSessions().size());
        assertEquals(1, resp.getSessions().get(0).getExerciseCount());
    }

    @Test
    void create_whenNameExists_throwsConflict() {
        when(planRepository.findByName("Plan")).thenReturn(Optional.of(plan));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createTrainingPlan(plan));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void create_whenValid_saves() {
        when(planRepository.findByName("Plan")).thenReturn(Optional.empty());
        when(planRepository.save(any(TrainingPlan1.class))).thenAnswer(i -> i.getArgument(0));

        TrainingPlan1 saved = service.createTrainingPlan(plan);

        assertEquals("Plan", saved.getName());
        verify(planRepository).save(plan);
    }

    @Test
    void update_whenNotFound_throws() {
        when(planRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateTrainingPlan(9L, null));
        assertTrue(ex.getMessage().contains("TrainingPlan not found"));
    }

    @Test
    void update_whenNameConflict_throwsConflict() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.findByNameAndIdNot("Other", 1L)).thenReturn(Optional.of(TrainingPlan1.builder().id(2L).name("Other").build()));

        TrainingPlanRequest req = new TrainingPlanRequest();
        req.setName("Other");
        req.setDescription("D");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.updateTrainingPlan(1L, req));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void update_whenValid_saves() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.findByNameAndIdNot("PlanNew", 1L)).thenReturn(Optional.empty());
        when(planRepository.save(any(TrainingPlan1.class))).thenAnswer(i -> i.getArgument(0));

        TrainingPlanRequest req = new TrainingPlanRequest();
        req.setName("PlanNew");
        req.setDescription("NewDesc");

        var updated = service.updateTrainingPlan(1L, req);
        assertEquals("PlanNew", updated.getName());
        verify(planRepository).save(any());
    }

    @Test
    void delete_clearsSessionsAndDeletesPlan() {
        TrainingSession1 s = TrainingSession1.builder().id(3L).trainingPlan(plan).build();
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(sessionRepository.findByTrainingPlan_IdOrderByOrderIndexAsc(1L)).thenReturn(List.of(s));

        service.deleteTrainingPlan(1L);

        assertNull(s.getTrainingPlan());
        verify(sessionRepository).saveAll(any());
        verify(planRepository).delete(plan);
    }
}
