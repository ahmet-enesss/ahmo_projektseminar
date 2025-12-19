package com.example.fitnessapp.service;

import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Model.TrainingSessionStatus;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import com.example.fitnessapp.Service.TrainingSessionService1;
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
// Ermöglicht das Mocken des Repositories und das automatische Injizieren in den Service
@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceUnitTest {

    @Mock // Mock für das TrainingSessionRepository
    // Simuliert Datenbankzugriffe auf Trainingssessions
    private TrainingSessionRepository1 sessionRepository;

    @InjectMocks // Das zu testende Service-Objekt
    // Mockito injiziert automatisch das Mock-Repository
    private TrainingSessionService1 service;
    private TrainingSession1 session;

    // Wird vor jedem Test ausgeführt
    // Erstellt eine gültige Trainingssession, die in mehreren Tests verwendet wird
    @BeforeEach
    void setUp() {
        session = TrainingSession1.builder()
                .id(5L)
                .name("S")
                .build();
    }

    // Testfall:
    // Das Repository liefert eine Liste von Trainingssessions zurück
    // --> Der Service soll diese Liste unverändert weitergeben
    @Test
    void getAll_returnsList() {
        when(sessionRepository.findAll()).thenReturn(List.of(session));
        var all = service.getAllTrainingSessions();
        // Erwartung:Genau eine Trainingssession wird zurückgegeben.
        assertEquals(1, all.size());
    }

    // Testfall:
    // Eine Trainingssession mit der angegebenen ID existiert nicht
    // --> Der Service muss eine Exception werfen (404)
    @Test
    void getById_notFound_throws() {
        when(sessionRepository.findById(9L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getTrainingSessionById(9L)
        );
        // Überprüfung der Fehlermeldung
        assertTrue(ex.getMessage().contains("TrainingSession not found"));
    }

    // Testfall:
    // Die createTrainingSession-Methode ist als veraltet markiert
    // --> Jeder Aufruf muss mit einer BAD_REQUEST-Exception abgelehnt werden
    @Test
    void deprecatedCreate_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.createTrainingSession(
                        1L,
                        "n",
                        null,
                        TrainingSessionStatus.GEPLANT,
                        Set.of()
                )
        );
        // Erwartung:Fehlermeldung weist darauf hin, dass diese Methode veraltet ist.
        assertTrue(ex.getMessage().contains("veraltet"));
    }

    // Testfall:
    // Eine Trainingssession soll gelöscht werden
    // --> Der Service delegiert direkt an das Repository
    @Test
    void delete_callsRepository() {
        service.deleteTrainingSession(4L);
        // Überprüft, ob deleteById mit der richtigen ID aufgerufen wurde.
        verify(sessionRepository).deleteById(4L);
    }
}