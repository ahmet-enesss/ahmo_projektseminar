package com.example.fitnessapp.service;

import com.example.fitnessapp.DTOs.ExerciseRequest;
import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import com.example.fitnessapp.Service.ExerciseService1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Aktiviert Mockito für JUnit 5
// Erlaubt das Mocken des Repositories und die automatische Injection in den Service
@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock  // Mock für den Zugriff auf die Exercise-Tabelle
    private ExerciseRepository1 exerciseRepository;

    @InjectMocks// Das zu testende Service-Objekt
    // Mockito injiziert das gemockte Repository automatisch
    private ExerciseService1 exerciseService;
    private ExerciseRequest validRequest;
    private Exercise1 exercise;

    //Gültige Standard-Request die in mehreren Tests wiederverwendet wird
    @BeforeEach
    void setUp() {
        validRequest = new ExerciseRequest("Bankdrücken", "Kraft", Set.of("Brust"), "Desc");
        // Beispiel-Exercise aus der „Datenbank“
        exercise = Exercise1.builder().id(1L).name("Bankdrücken").build();
    }

    // Test: Eine Übung mit gültigen Daten wird erstellt
    // --> Es existiert noch keine Übung mit diesem Namen
    @Test
    void shouldCreateExerciseSuccessfullyWhenRequestIsValid() {
        when(exerciseRepository.findByName("Bankdrücken")).thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise1.class))).thenAnswer(i -> i.getArgument(0));

        Exercise1 created = exerciseService.createExercise(validRequest);

        assertNotNull(created);
        assertEquals("Bankdrücken", created.getName());
        verify(exerciseRepository).save(any());
    }

    // Test:Eine Übung mit dem gleichen Namen existiert bereits
    // --> Der Service muss einen CONFLICT zurückgeben
    @Test
    void shouldThrowConflictExceptionWhenExerciseNameAlreadyExists() {
        when(exerciseRepository.findByName("Bankdrücken")).thenReturn(Optional.of(exercise));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // Test:Der Name besteht nur aus Leerzeichen
    // -->Testet die isBlank()-Validierung
    @Test
    void shouldThrowBadRequestExceptionWhenNameIsBlank() {
        validRequest.setName("  ");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // Test:Die Menge der Muskelgruppen ist leer
    // --> Eine Übung ohne Muskelgruppen ist nicht erlaubt
    @Test
    void shouldThrowBadRequestExceptionWhenMuscleGroupsAreMissing() {
        validRequest.setMuscleGroups(Set.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // Test:Eine Übung wird erfolgreich per ID gefunden
    @Test
    void shouldReturnExerciseWhenIdExists() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        Exercise1 result = exerciseService.getExerciseById(1L);
        assertEquals("Bankdrücken", result.getName());
    }

    // Test:Die angefragte Übung existiert nicht
    // --> Der Service muss NOT_FOUND werfen
    @Test
    void shouldThrowNotFoundExceptionWhenExerciseDoesNotExist() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> exerciseService.getExerciseById(99L));
    }

    // Test(Happy Path):
    @Test
    void shouldCreateExerciseSuccessfully() {
        when(exerciseRepository.findByName("Bankdrücken")).thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise1.class))).thenAnswer(i -> i.getArgument(0));
        Exercise1 created = exerciseService.createExercise(validRequest);
        assertNotNull(created);
        verify(exerciseRepository).save(any());
    }

    // Test:Eine bestehende Übung wird erfolgreich aktualisiert
    // Kein Namenskonflikt mit anderen Übungen
    @Test
    void updateExercise_shouldUpdateWhenValid() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.findByNameAndIdNot("Neuer Name", 1L)).thenReturn(Optional.empty());
        when(exerciseRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExerciseRequest updateReq = new ExerciseRequest("Neuer Name", "Kraft", Set.of("Beine"), "Desc");
        Exercise1 updated = exerciseService.updateExercise(1L, updateReq);

        assertEquals("Neuer Name", updated.getName());
        verify(exerciseRepository).save(any());
    }
    // Test:Beim Update existiert bereits eine andere Übung mit gleichem Namen
    // --> Der Service muss einen CONFLICT werfen
    @Test
    void updateExercise_shouldThrowConflictWhenNameExists() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.findByNameAndIdNot("Existiert", 1L)).thenReturn(Optional.of(new Exercise1()));

        ExerciseRequest updateReq = new ExerciseRequest("Existiert", "Kraft", Set.of("Beine"), "Desc");

        assertThrows(ResponseStatusException.class, () -> exerciseService.updateExercise(1L, updateReq));
    }

    // Test: Eine Übung wird gelöscht
    // Der Service delegiert korrekt an das Repository
    @Test
    void deleteExercise_shouldCallDelete() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        exerciseService.deleteExercise(1L);
        verify(exerciseRepository).delete(exercise);
    }

    // Testfall:Die Kategorie ist leer
    // Testet den zweiten Teil der (null || blank)-Bedingung
    @Test
    void shouldThrowBadRequestExceptionWhenCategoryIsBlank() {
        validRequest.setCategory(""); // Testet den zweiten Teil der || Bedingung
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // Test:Die Kategorie ist null
    // Testet den ersten Teil der (null || blank)-Bedingung
    @Test
    void shouldThrowBadRequestExceptionWhenCategoryIsNull() {
        validRequest.setCategory(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}