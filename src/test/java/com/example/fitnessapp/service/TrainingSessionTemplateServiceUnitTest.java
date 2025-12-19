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

// Aktiviert Mockito für JUnit 5
// Ermöglicht das Mocken aller Repositories und das Injizieren in den Service
@ExtendWith(MockitoExtension.class)
class TrainingSessionTemplateServiceUnitTest {

    @Mock // Mock für Trainingssessions (CRUD + Abfragen)
    private TrainingSessionRepository1 sessionRepository;

    @Mock // Mock für Trainingspläne
    private TrainingPlanRepository1 planRepository;

    @Mock // Mock für Übungsvorlagen innerhalb einer Session
    private ExerciseExecutionTemplateRepository exerciseTemplateRepository;

    @Mock
    // Mock für Session-Logs
    private SessionLogRepository sessionLogRepository;

    // Das zu testende Service-Objekt
    // Mockito injiziert automatisch alle oben definierten Mocks
    @InjectMocks
    private TrainingSessionTemplateService service;

    // Testfall:
    // Der Name ist leer
    // --> Laut Validierungslogik ist ein leerer Name nicht erlaubt
    @Test
    void shouldThrowExceptionWhenSessionNameIsMissing() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("");
        assertThrows(ResponseStatusException.class,
                () -> service.createSession(req)
        );
    }

    // Testfall:
    // Der orderIndex ist größer als erlaubt
    // --> Der Service muss dies ablehnen
    @Test
    void shouldThrowExceptionWhenOrderIndexIsInvalid() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Session");
        req.setOrderIndex(31); // Maximal erlaubt ist 30
        assertThrows(ResponseStatusException.class,
                () -> service.createSession(req)
        );
    }

    // Testfall:
    // Ein Trainingsplan hat bereits 30 Sessions
    // --> Es darf keine weitere Session hinzugefügt werden
    @Test
    void shouldThrowExceptionWhenPlanHasMaxSessions() {

        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setPlanId(1L);
        req.setName("X");
        req.setOrderIndex(1);

        when(planRepository.findById(1L)).thenReturn(Optional.of(new TrainingPlan1()));
        when(sessionRepository.countByTrainingPlan_Id(1L)).thenReturn(30L);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.createSession(req)
        );
        // Überprüfung der Fehlermeldung
        assertTrue(ex.getReason().contains("maximal 30 Sessions"));
    }

    // Testfall:
    // Der orderIndex ist im Trainingsplan bereits vergeben
    // --> Der Service muss einen Konflikt melden

    @Test
    void shouldThrowConflictExceptionWhenOrderIndexAlreadyUsed() {

        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setPlanId(1L);
        req.setOrderIndex(5);
        req.setName("New");

        when(planRepository.findById(1L)).thenReturn(Optional.of(new TrainingPlan1()));
        when(sessionRepository.countByTrainingPlan_Id(1L)).thenReturn(5L);
        when(sessionRepository.findByTrainingPlan_IdAndOrderIndex(1L, 5))
                .thenReturn(Optional.of(new TrainingSession1()));
        assertThrows(
                ResponseStatusException.class,
                () -> service.createSession(req)
        );
    }
    // Testfall:
    // Die zu aktualisierende Session existiert nicht
    //--> Der Service muss eine NOT_FOUND-Exception werfen
    @Test
    void updateSession_shouldThrowNotFoundWhenIdInvalid() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Test");
        req.setOrderIndex(1);
        assertThrows(ResponseStatusException.class,
                () -> service.updateSession(99L, req)
        );
    }

    // Testfall:
    // Eine Session wird ohne Trainingsplan erstellt
    // --> Dieser Branch ist wichtig da planId optional ist
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
        // Überprüfung der Rückgabewerte
        assertEquals("Freies Training", resp.getName());
        assertNull(resp.getPlanId());
    }
    // Testfall:
    // Eine bestehende Session wird aktualisiert und explizit keinem Trainingsplan zugeordnet
    @Test
    void updateSession_shouldWorkWithoutPlan() {
        TrainingSession1 session = TrainingSession1.builder()
                .id(1L)
                .name("Old")
                .orderIndex(1)
                .build();

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

    // Testfall:
    // Der Name ist null
    // --> Testet explizit die Null-Prüfung im Service
    @Test
    void createSession_shouldThrowWhenNameIsNull() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName(null);

        assertThrows(
                ResponseStatusException.class,
                () -> service.createSession(req)
        );
    }
    // Testfall:
    // Der orderIndex ist kleiner als 1
    // --> Dieser Wert ist laut Regel ungültig
    @Test
    void createSession_shouldThrowWhenOrderIndexTooLow() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Test");
        req.setOrderIndex(0); // Testet: if (orderIndex < 1)
        assertThrows(ResponseStatusException.class, () -> service.createSession(req));
    }

    // Testfall:
    // Der Name besteht nur aus Leerzeichen
    // Testet die isBlank()-Validierung

    @Test
    void updateSession_shouldThrowWhenNameIsBlank() {
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("  "); // Testet: if (request.getName().isBlank())
        req.setOrderIndex(5);

        TrainingSession1 session = TrainingSession1.builder().id(1L).build();
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThrows(ResponseStatusException.class, () -> service.updateSession(1L, req));
    }


    // Testfall:
    // Eine Session wird gelöscht
    // --> Der Service delegiert korrekt an das Repository
    @Test
    void deleteSession_shouldCallRepository() {

        TrainingSession1 session = TrainingSession1.builder().id(1L).build();
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        service.deleteSession(1L);

        verify(sessionRepository).delete(session);
    }

    // Testfall:
    // Der orderIndex fehlt
    // --> Dieser Branch verhindert inkonsistente Daten
    @Test
    void updateSession_shouldThrowWhenOrderIndexIsNull() {
        TrainingSession1 session = TrainingSession1.builder().id(1L).build();
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Valid Name");
        req.setOrderIndex(null); // Testet: if (request.getOrderIndex() == null)
        assertThrows(ResponseStatusException.class,
                () -> service.updateSession(1L, req)
        );
    }

    // Testfall:
    // Der Name ist null beim Update
    // --> Muss abgelehnt werden
    @Test
    void updateSession_shouldThrowWhenNameIsNull() {
        TrainingSession1 session = TrainingSession1.builder().id(1L).build();
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName(null); // Testet: if (request.getName() == null)
        req.setOrderIndex(5);

        assertThrows(ResponseStatusException.class,
                () -> service.updateSession(1L, req)
        );
    }

    // Testfall:
    // Der orderIndex überschreitet das Maximum von 30.
    @Test
    void updateSession_shouldThrowWhenOrderIndexTooHigh() {

        TrainingSession1 session = TrainingSession1.builder().id(1L).build();
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Test");
        req.setOrderIndex(33);  // Testet: if (request.getOrderIndex() > 30)

        assertThrows(ResponseStatusException.class,
                () -> service.updateSession(1L, req)
        );
    }
    // Testfall:
    // Eine planId wird angegeben der Trainingsplan existiert aber nicht
    // --> Der Service muss dies ablehnen
    @Test
    void updateSession_shouldThrowWhenPlanNotFound() {

        TrainingSession1 session = TrainingSession1.builder().id(1L).build();
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(planRepository.findById(99L)).thenReturn(Optional.empty());
        TrainingSessionTemplateRequest req = new TrainingSessionTemplateRequest();
        req.setName("Test");
        req.setOrderIndex(1);
        req.setPlanId(99L);

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateSession(1L, req)
        );
    }
}