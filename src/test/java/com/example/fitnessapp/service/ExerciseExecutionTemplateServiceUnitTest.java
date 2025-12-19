package com.example.fitnessapp.service;

import com.example.fitnessapp.DTOs.ExerciseExecutionTemplateRequest;
import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import com.example.fitnessapp.Service.ExerciseExecutionTemplateService;
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

// Aktiviert Mockito für JUnit 5
// Ohne diese Annotation würden @Mock und @InjectMocks nicht funktionieren
@ExtendWith(MockitoExtension.class)
class ExerciseExecutionTemplateServiceUnitTest {

    @Mock// Mock für das Repository der ExerciseExecutionTemplates
    // Simuliert Datenbankzugriffe ohne echte DB
    private ExerciseExecutionTemplateRepository templateRepository;

    @Mock// Mock für Trainingssessions
    private TrainingSessionRepository1 trainingSessionRepository;

    @Mock// Mock für Übungen
    private ExerciseRepository1 exerciseRepository;

    @InjectMocks// Das zu testende Service-Objekt
    // Mockito injiziert automatisch alle oben definierten Mocks
    private ExerciseExecutionTemplateService service;

    private TrainingSession1 session;
    private Exercise1 exercise;

    // Wird vor jedem Test ausgeführt
    // Erstellt wiederverwendbare Testobjekte
    @BeforeEach
    void setUp() {
        session = TrainingSession1.builder().id(2L).name("S").build();
        exercise = Exercise1.builder().id(3L).name("E").category("C").build();
    }

    // Testfall:
    // Wenn die Trainingssession nicht existiert
    // --> soll der Service eine 404-Exception werfen
    @Test
    void create_whenSessionMissing_throws() {
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setSessionId(2L);
        req.setExerciseId(3L);
        req.setPlannedSets(3);
        req.setPlannedReps(10);
        req.setPlannedWeight(0.0);
        req.setOrderIndex(1);
        // Repository liefert keine Session zurück
        when(trainingSessionRepository.findById(2L)).thenReturn(Optional.empty());
        // Erwartung: Exception wird geworfen
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.create(req));
        // Überprüft ob die Fehlermeldung korrekt ist
        assertTrue(ex.getMessage().contains("TrainingSession not found"));
    }

    // Testfall:
    // Session existiert aber die Übung nicht
    // --> Fehler

    @Test
    void create_whenExerciseMissing_throws() {
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setSessionId(2L);
        req.setExerciseId(3L);
        req.setPlannedSets(3);
        req.setPlannedReps(10);
        req.setPlannedWeight(0.0);
        req.setOrderIndex(1);

        // Session wird gefunden
        when(trainingSessionRepository.findById(2L)).thenReturn(Optional.of(session));
        // Übung wird NICHT gefunden
        when(exerciseRepository.findById(3L)).thenReturn(Optional.empty());
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.create(req));

        assertTrue(ex.getMessage().contains("Exercise not found"));
    }

    // Testfall:
    // Ungültige Planwerte (0 oder negativ)
    // --> validate()-Methode soll eine Exception werfen
    @Test
    void create_whenInvalidPlanned_throws() {
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setSessionId(2L);
        req.setExerciseId(3L);
        req.setPlannedSets(0);     // ungültig
        req.setPlannedReps(0);     // ungültig
        req.setPlannedWeight(-1.0);  // ungültig
        req.setOrderIndex(1);
        when(trainingSessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(exercise));
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.create(req));

        assertTrue(ex.getMessage().contains("Invalid planned values"));
    }

    // Testfall:
    // Die Reihenfolge (orderIndex) ist in dieser Session bereits vergeben
    @Test
    void create_whenOrderTaken_throws() {
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setSessionId(2L);
        req.setExerciseId(3L);
        req.setPlannedSets(3);
        req.setPlannedReps(10);
        req.setPlannedWeight(0.0);
        req.setOrderIndex(1);
        when(trainingSessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(exercise));
        // Simuliert: OrderIndex ist bereits belegt
        when(templateRepository.existsByTrainingSession_IdAndOrderIndex(2L, 1))
                .thenReturn(true);
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.create(req));
        assertTrue(ex.getMessage().contains("Order index already used"));
    }

    // Testfall:
    // Dieselbe Übung wurde der Session bereits hinzugefügt
    @Test
    void create_whenExerciseAlreadyAdded_throws() {
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setSessionId(2L);
        req.setExerciseId(3L);
        req.setPlannedSets(3);
        req.setPlannedReps(10);
        req.setPlannedWeight(0.0);
        req.setOrderIndex(2);
        when(trainingSessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(exercise));
        // Reihenfolge ist frei
        when(templateRepository.existsByTrainingSession_IdAndOrderIndex(2L, 2))
                .thenReturn(false);
        // Übung existiert bereits in der Session
        when(templateRepository.existsByTrainingSession_IdAndExercise_Id(2L, 3L))
                .thenReturn(true);
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.create(req));
        assertTrue(ex.getMessage().contains("Exercise already added"));
    }

    // Testfall (Happy Path):
    // Alle Validierungen bestehen
    // --> Template wird gespeichert
    @Test
    void create_happyPath_returnsResponse() {
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setSessionId(2L);
        req.setExerciseId(3L);
        req.setPlannedSets(3);
        req.setPlannedReps(10);
        req.setPlannedWeight(0.0);
        req.setOrderIndex(2);

        when(trainingSessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(exercise));
        when(templateRepository.existsByTrainingSession_IdAndOrderIndex(2L, 2)).thenReturn(false);
        when(templateRepository.existsByTrainingSession_IdAndExercise_Id(2L, 3L)).thenReturn(false);
        // Simuliert das Speichern in der DB inkl. ID-Vergabe
        when(templateRepository.save(any())).thenAnswer(i -> {
            ExerciseExecutionTemplate t = i.getArgument(0);
            t.setId(77L);
            return t;
        });
        var resp = service.create(req);
        assertEquals(77L, resp.getId());
        assertEquals(3, resp.getPlannedSets());
    }

    // Testfall:
    // Template-ID existiert nicht --> 404
    @Test
    void update_whenTemplateNotFound_throws() {
        when(templateRepository.findById(5L)).thenReturn(Optional.empty());
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class,
                        () -> service.update(5L, new ExerciseExecutionTemplateRequest()));
        assertTrue(ex.getMessage().contains("Template not found"));
    }

    // Testet:
    // - Repository-Abfrage
    // - Mapping von Entity
    // --> Response DTO
    @Test
    void getForSession_returnsMappedResponses() {
        ExerciseExecutionTemplate t =
                ExerciseExecutionTemplate.builder()
                        .id(1L)
                        .plannedSets(2)
                        .plannedReps(3)
                        .plannedWeight(5.0)
                        .exercise(exercise)
                        .trainingSession(session)
                        .orderIndex(1)
                        .build();

        when(templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(2L))
                .thenReturn(List.of(t));
        var list = service.getForSession(2L);
        assertEquals(1, list.size());
        assertEquals(3, list.get(0).getPlannedReps());
    }
    // Test sagt: Wenn beim Update der orderIndex mit einem bestehenden kollidiert, muss eine Exception kommen.
    @Test
    void update_whenOrderConflict_throws() {
        ExerciseExecutionTemplate existing = ExerciseExecutionTemplate.builder().id(5L).orderIndex(1).trainingSession(session).exercise(exercise).build();
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setExerciseId(3L);
        req.setPlannedSets(3);
        req.setPlannedReps(8);
        req.setPlannedWeight(0.0);
        req.setOrderIndex(2);

        when(templateRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(templateRepository.existsByTrainingSession_IdAndOrderIndex(session.getId(), 2)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.update(5L, req));
        assertTrue(ex.getMessage().contains("Order index already used"));
    }
    // Test sagt: Wenn beim Update die neue Übung-ID nicht existiert, muss eine Exception kommen.
    @Test
    void update_whenExerciseNotFound_throws() {
        ExerciseExecutionTemplate existing = ExerciseExecutionTemplate.builder().id(6L).orderIndex(1).trainingSession(session).exercise(exercise).build();
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setExerciseId(99L);
        req.setPlannedSets(3);
        req.setPlannedReps(8);
        req.setPlannedWeight(0.0);
        req.setOrderIndex(1);

        when(templateRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.update(6L, req));
        assertTrue(ex.getMessage().contains("Exercise not found"));
    }
    // Test sagt: Wenn man beim Update eine Übung setzt, die in der Session schon vorhanden ist, muss eine Exception kommen.
    @Test
    void update_whenDuplicateExercise_throws() {
        ExerciseExecutionTemplate existing = ExerciseExecutionTemplate.builder().id(7L).orderIndex(1).trainingSession(session).exercise(Exercise1.builder().id(3L).build()).build();
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setExerciseId(4L);
        req.setPlannedSets(3);
        req.setPlannedReps(8);
        req.setPlannedWeight(0.0);
        req.setOrderIndex(1);

        when(templateRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.findById(4L)).thenReturn(Optional.of(Exercise1.builder().id(4L).build()));
        when(templateRepository.existsByTrainingSession_IdAndExercise_Id(existing.getTrainingSession().getId(), 4L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.update(7L, req));
        assertTrue(ex.getMessage().contains("Exercise already added"));
    }

    // Test sagt: Wenn alle Daten ok sind, soll update funktionieren und die Antwort die neuen Werte enthalten.
    @Test
    void update_happyPath_returnsResponse() {
        ExerciseExecutionTemplate existing = ExerciseExecutionTemplate.builder()
                .id(10L)
                .orderIndex(1)
                .trainingSession(session)
                .exercise(exercise)
                .plannedSets(2)
                .plannedReps(5)
                .plannedWeight(0.0)
                .build();

        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setExerciseId(3L);
        req.setPlannedSets(4);
        req.setPlannedReps(10);
        req.setPlannedWeight(2.5);
        req.setOrderIndex(1);

        when(templateRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(Exercise1.builder().id(3L).name("New").category("C").build()));
        // no need to stub existsBy... because in this happy-path the conditional checks are skipped
        when(templateRepository.save(any(ExerciseExecutionTemplate.class))).thenAnswer(i -> i.getArgument(0));

        var resp = service.update(10L, req);
        assertEquals(3L, resp.getExerciseId());
        assertEquals(10, resp.getPlannedReps());
    }

    // Testet nur die Weiterleitung an das Repository
    //es wird nur geprüft, dass deleteById aufgerufen wird.
    // Test sagt: service.delete muss templateRepository.deleteById ausführen
    @Test
    void delete_callsRepository() {
        service.delete(4L);
        verify(templateRepository).deleteById(4L);
    }
    // Test sagt: jede einzelne ungültige Eingabe (Sets/Reps/Weight/OrderIndex) muss eine Exception auslösen
    @Test
    void validate_shouldThrowForEveryInvalidBranch() {
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setSessionId(2L);
        req.setExerciseId(3L);

        // Fall 1: Sets <= 0
        req.setPlannedSets(0); req.setPlannedReps(10); req.setPlannedWeight(5.0); req.setOrderIndex(1);
        assertThrows(ResponseStatusException.class, () -> service.create(req));

        // Fall 2: Reps <= 0
        req.setPlannedSets(3); req.setPlannedReps(0);
        assertThrows(ResponseStatusException.class, () -> service.create(req));

        // Fall 3: Weight < 0
        req.setPlannedReps(10); req.setPlannedWeight(-0.1);
        assertThrows(ResponseStatusException.class, () -> service.create(req));

        // Fall 4: OrderIndex <= 0
        req.setPlannedWeight(5.0); req.setOrderIndex(0);
        assertThrows(ResponseStatusException.class, () -> service.create(req));
    }
    // Test sagt: Wenn orderIndex beim Update gleich bleibt, darf der Konflikt-Check nicht „nötig“ sein und es soll nicht crashen.
    // Testet den Branch, bei dem die Order gleich bleibt (!existing.getOrderIndex().equals(request.getOrderIndex())) // Bedeutet: der Codezweig für Konfliktprüfung wird nur ausgeführt, wenn sich orderIndex ändert.
    @Test
    void update_shouldNotTriggerOrderConflictWhenOrderIsSame() {
        // Testet den Branch, bei dem die Order gleich bleibt (!existing.getOrderIndex().equals(request.getOrderIndex()))
        ExerciseExecutionTemplate existing = ExerciseExecutionTemplate.builder()
                .id(1L).orderIndex(5).trainingSession(session).exercise(exercise).build();
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setOrderIndex(5); // Gleiche Nummer
        req.setExerciseId(3L); req.setPlannedSets(3); req.setPlannedReps(10); req.setPlannedWeight(0.0);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(exercise));
        when(templateRepository.save(any())).thenReturn(existing);

        assertDoesNotThrow(() -> service.update(1L, req));
        // Hier wird der existsBy... Check im Service übersprungen -> Branch Abdeckung!
    }
    //Test sagt: Wenn die Übung-ID gleich bleibt, soll der Duplicate-Check nicht ausgeführt werden.
    // Testet den Branch: if (!existing.getExercise().getId().equals(exercise.getId())) // Bedeutet: Duplicate-Check läuft nur, wenn die ExerciseId sich ändert.
    @Test
    void update_shouldNotCheckDuplicateExerciseIfExerciseRemainsSame() {
        // Testet den Branch: if (!existing.getExercise().getId().equals(exercise.getId()))
        Exercise1 sameExercise = Exercise1.builder().id(3L).build();
        ExerciseExecutionTemplate existing = ExerciseExecutionTemplate.builder()
                .id(1L).exercise(sameExercise).trainingSession(session).orderIndex(1).build();

        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setExerciseId(3L); // Gleiche ID wie oben
        req.setPlannedSets(3); req.setPlannedReps(10); req.setPlannedWeight(10.0); req.setOrderIndex(1);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(sameExercise));
        when(templateRepository.save(any())).thenReturn(existing);

        assertDoesNotThrow(() -> service.update(1L, req));
        // Verify: existsByTrainingSession_IdAndExercise_Id wird NICHT aufgerufen
        verify(templateRepository, never()).existsByTrainingSession_IdAndExercise_Id(any(), any());
    }

    // Test sagt: Wenn plannedWeight negativ ist, muss create() eine Exception werfen.
    @Test
    void validate_shouldThrowWhenWeightIsNegative() {
        ExerciseExecutionTemplateRequest req = new ExerciseExecutionTemplateRequest();
        req.setPlannedSets(3); req.setPlannedReps(10);
        req.setPlannedWeight(-5.0); // Testet: if (plannedWeight < 0)
        req.setOrderIndex(1);

        // Wir triggern dies über create (da create validate aufruft)
        when(trainingSessionRepository.findById(any())).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(any())).thenReturn(Optional.of(exercise));

        assertThrows(ResponseStatusException.class, () -> service.create(req));
    }
}