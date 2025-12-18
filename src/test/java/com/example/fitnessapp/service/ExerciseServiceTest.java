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

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository1 exerciseRepository;

    @InjectMocks
    private ExerciseService1 exerciseService;

    private ExerciseRequest validRequest;
    private Exercise1 exercise;

    @BeforeEach
    void setUp() {
        validRequest = new ExerciseRequest("Bankdrücken", "Kraft", Set.of("Brust"), "Desc");
        exercise = Exercise1.builder().id(1L).name("Bankdrücken").build();
    }

    @Test
    void shouldCreateExerciseSuccessfullyWhenRequestIsValid() {
        when(exerciseRepository.findByName("Bankdrücken")).thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise1.class))).thenAnswer(i -> i.getArgument(0));

        Exercise1 created = exerciseService.createExercise(validRequest);

        assertNotNull(created);
        assertEquals("Bankdrücken", created.getName());
        verify(exerciseRepository).save(any());
    }

    @Test
    void shouldThrowConflictExceptionWhenExerciseNameAlreadyExists() {
        when(exerciseRepository.findByName("Bankdrücken")).thenReturn(Optional.of(exercise));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenNameIsBlank() {
        validRequest.setName("  ");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenMuscleGroupsAreMissing() {
        validRequest.setMuscleGroups(Set.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldReturnExerciseWhenIdExists() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        Exercise1 result = exerciseService.getExerciseById(1L);
        assertEquals("Bankdrücken", result.getName());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenExerciseDoesNotExist() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> exerciseService.getExerciseById(99L));
    }

    @Test
    void shouldCreateExerciseSuccessfully() {
        when(exerciseRepository.findByName("Bankdrücken")).thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise1.class))).thenAnswer(i -> i.getArgument(0));

        Exercise1 created = exerciseService.createExercise(validRequest);

        assertNotNull(created);
        verify(exerciseRepository).save(any());
    }

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

    @Test
    void updateExercise_shouldThrowConflictWhenNameExists() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.findByNameAndIdNot("Existiert", 1L)).thenReturn(Optional.of(new Exercise1()));

        ExerciseRequest updateReq = new ExerciseRequest("Existiert", "Kraft", Set.of("Beine"), "Desc");

        assertThrows(ResponseStatusException.class, () -> exerciseService.updateExercise(1L, updateReq));
    }

    @Test
    void deleteExercise_shouldCallDelete() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        exerciseService.deleteExercise(1L);

        verify(exerciseRepository).delete(exercise);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCategoryIsBlank() {
        validRequest.setCategory(""); // Testet den zweiten Teil der || Bedingung
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCategoryIsNull() {
        validRequest.setCategory(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}