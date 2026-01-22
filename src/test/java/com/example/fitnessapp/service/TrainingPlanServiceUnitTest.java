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
import org.junit.jupiter.api.Disabled;
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
// Aktiviert Mockito für JUnit 5
// Ermöglicht das Mocken der Repositories und das automatische Injizieren in den Service
@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceUnitTest {

    @Mock// Mock für das TrainingPlan-Repository (CRUD für Trainingspläne)
    private TrainingPlanRepository1 planRepository;

    @Mock// Mock für Trainingssessions die zu einem Trainingsplan gehören
    private TrainingSessionRepository1 sessionRepository;

    @Mock// Mock für Übungstemplates innerhalb von Sessions
    private ExerciseExecutionTemplateRepository templateRepository;

    @Mock
    private com.example.fitnessapp.Repository.TrainingPlanSessionTemplateRepository planTemplateRepository;

    @InjectMocks// Das zu testende Service-Objekt
    // Mockito injiziert automatisch alle oben definierten Mocks
    private TrainingPlanService1 service;
    private TrainingPlan1 plan;

    // Wird vor jedem Test ausgeführt
    // Erstellt einen gültigen Trainingsplan für mehrere Tests
    @BeforeEach
    void setUp() {
        plan = TrainingPlan1.builder()
                .id(1L)
                .name("Plan")
                .description("D")
                .build();
    }

    // Testfall:
    // Es existiert ein Trainingsplan
    // --> Der Service soll eine Übersicht zurückgeben inkl. Anzahl Sessions
    @Test
    void getAllTrainingPlans_returnsOverview() {
        when(planRepository.findAll()).thenReturn(List.of(plan));
        when(sessionRepository.countByTrainingPlan_Id(1L)).thenReturn(2L);
        var list = service.getAllTrainingPlans();
        // Erwartung:Genau ein Plan in der Liste
        assertEquals(1, list.size());
        // Die Anzahl der Sessions wird korrekt gemappt
        assertEquals(2L, list.get(0).getSessionCount());
    }
    // Testfall:
    // Trainingsplan mit dieser ID existiert nicht
    // --> Der Service muss eine Exception werfen (404)
    @Test
    void getTrainingPlanById_whenNotFound_throws() {
        when(planRepository.findById(5L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getTrainingPlanById(5L)
        );
        assertTrue(ex.getMessage().contains("TrainingPlan not found"));
    }

    // Testfall:
    // Trainingsplan existiert hat aber noch KEINE Sessions
    // Der Service soll:
    //  --> hasSessions = false setzen
    //  --> einen Hinweistext zurückgeben
    @Test
    void getTrainingPlanById_returnsDetail_withSessionsHint() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(sessionRepository.findByTrainingPlan_IdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of());
        TrainingPlanDetailResponse resp = service.getTrainingPlanById(1L);
        assertFalse(resp.isHasSessions());
        assertTrue(resp.getSessionsHint().contains("Noch keine"));
    }

    // Testfall:
    // Trainingsplan besitzt mindestens eine Session.
    // Zusätzlich werden die Übungstemplates pro Session gezählt.
    @Test
    void getTrainingPlanById_withSessions_returnsHasSessionsTrueAndSummaries() {
        TrainingSession1 s = TrainingSession1.builder()
                .id(7L)
                .name("S1")
                .orderIndex(1)
                .build();

        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(sessionRepository.findByTrainingPlan_IdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(s));
        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(7L))
                .thenReturn(List.of(ExerciseExecutionTemplate.builder().id(2L).build()));

        TrainingPlanDetailResponse resp = service.getTrainingPlanById(1L);
        assertTrue(resp.isHasSessions());
        assertEquals(1, resp.getSessions().size());
        assertEquals(1, resp.getSessions().get(0).getExerciseCount());
    }

    // Testfall:
    // Ein Trainingsplan mit gleichem Namen existiert bereits
    // --> Der Service muss einen Conflict-Fehler werfen
    @Test
    void create_whenNameExists_throwsConflict() {
        when(planRepository.findByName("Plan")).thenReturn(Optional.of(plan));
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.createTrainingPlan(plan)
        );
        assertTrue(ex.getMessage().contains("already exists"));
    }

    // Testfall (Happy Path):
    // Kein Plan mit diesem Namen existiert → Speichern erlaubt
    @Test
    void create_whenValid_saves() {
        when(planRepository.findByName("Plan")).thenReturn(Optional.empty());
        when(planRepository.save(any(TrainingPlan1.class)))
                .thenAnswer(i -> i.getArgument(0));

        TrainingPlan1 saved = service.createTrainingPlan(plan);
        assertEquals("Plan", saved.getName());
        verify(planRepository).save(plan);
    }

    // Testfall:
    // Trainingsplan mit ID existiert nicht → Exception
    @Test
    void update_whenNotFound_throws() {
        when(planRepository.findById(9L)).thenReturn(Optional.empty());
        TrainingPlanRequest req = new TrainingPlanRequest();
        req.setName("X");
        req.setDescription("D");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.updateTrainingPlan(9L, req)
        );
        assertTrue(ex.getMessage().contains("TrainingPlan not found"));
    }

    // Testfall:
    // Neuer Name kollidiert mit einem anderen existierenden Trainingsplan
    @Test
    void update_whenNameConflict_throwsConflict() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.findByNameAndIdNot("Other", 1L))
                .thenReturn(Optional.of(
                        TrainingPlan1.builder().id(2L).name("Other").build()
                ));
        TrainingPlanRequest req = new TrainingPlanRequest();
        req.setName("Other");
        req.setDescription("D");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.updateTrainingPlan(1L, req)
        );
        assertTrue(ex.getMessage().contains("already exists"));
    }

    // Testfall (Happy Path):
    // Trainingsplan existiert Name ist eindeutig → Update erlaubt
    @Test
    void update_whenValid_saves() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.findByNameAndIdNot("PlanNew", 1L))
                .thenReturn(Optional.empty());
        when(planRepository.save(any(TrainingPlan1.class)))
                .thenAnswer(i -> i.getArgument(0));
        TrainingPlanRequest req = new TrainingPlanRequest();
        req.setName("PlanNew");
        req.setDescription("NewDesc");
        var updated = service.updateTrainingPlan(1L, req);
        assertEquals("PlanNew", updated.getName());
        verify(planRepository).save(any());
    }

    // Testfall:
    // Beim Löschen eines Trainingsplans müssen:
    //  --> alle Sessions vom Plan entkoppelt werden
    //  --> der Plan selbst gelöscht werden
    @Test
    void delete_clearsSessionsAndDeletesPlan() {
        TrainingSession1 s = TrainingSession1.builder()
                .id(3L)
                .trainingPlan(plan)
                .build();
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(sessionRepository.findByTrainingPlan_IdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(s));
        service.deleteTrainingPlan(1L);
        // Trainingsplan-Referenz wurde entfernt
        assertNull(s.getTrainingPlan());
        // Sessions wurden gespeichert
        verify(sessionRepository).saveAll(any());
        // Trainingsplan wurde gelöscht
        verify(planRepository).delete(plan);
    }

    @Test
    void addTemplateToPlan_createsCopyAndCopiesExercises() {
        TrainingSession1 template = TrainingSession1.builder().id(20L).name("Tpl").orderIndex(1).build();
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(sessionRepository.findById(20L)).thenReturn(Optional.of(template));
        when(sessionRepository.countByTrainingPlan_Id(1L)).thenReturn(0L);
        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(20L))
                .thenReturn(List.of(ExerciseExecutionTemplate.builder().id(5L).build()));
        when(sessionRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(sessionRepository.save(any())).thenAnswer(i -> {
            TrainingSession1 s = i.getArgument(0);
            s.setId(99L);
            return s;
        });

        service.addTemplateToPlan(1L, 20L, null);

        verify(sessionRepository).save(any());
        verify(templateRepository).save(any());
    }

    @Test
    void removeTemplateFromPlan_deletesSessionAndExercises() {
        TrainingSession1 s = TrainingSession1.builder().id(30L).trainingPlan(plan).build();
        when(sessionRepository.findById(30L)).thenReturn(Optional.of(s));
        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(30L))
                .thenReturn(List.of(ExerciseExecutionTemplate.builder().id(7L).build()));

        service.removeTemplateFromPlan(1L, 30L);

        verify(templateRepository).deleteAll(anyList());
        verify(sessionRepository).delete(s);
    }
}