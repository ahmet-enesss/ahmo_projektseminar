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

import java.util.List;
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
        validRequest = new ExerciseRequest(
                "Push Up",
                "Strength",
                Set.of("Chest", "Triceps"),
                "Classic bodyweight exercise"
        );

        exercise = Exercise1.builder()
                .id(1L)
                .name("Push Up")
                .category("Strength")
                .muscleGroups(Set.of("Chest", "Triceps"))
                .description("Classic bodyweight exercise")
                .build();
    }

    // ---------- getAllExercises ----------

    @Test
    void shouldReturnAllExercises() {
        when(exerciseRepository.findAll()).thenReturn(List.of(exercise));

        List<Exercise1> result = exerciseService.getAllExercises();

        assertEquals(1, result.size());
        verify(exerciseRepository).findAll();
    }

    // ---------- getExerciseById ----------

    @Test
    void shouldReturnExerciseWhenIdExists() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        Exercise1 result = exerciseService.getExerciseById(1L);

        assertEquals("Push Up", result.getName());
    }

    @Test
    void shouldThrowNotFoundWhenExerciseDoesNotExist() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.getExerciseById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ---------- createExercise ----------

    @Test
    void shouldThrowBadRequestWhenNameIsMissing() {
        validRequest.setName("");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenCategoryIsMissing() {
        validRequest.setCategory("");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenMuscleGroupsMissing() {
        validRequest.setMuscleGroups(Set.of());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenNameIsNull() {
        validRequest.setName(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenCategoryIsNull() {
        validRequest.setCategory(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenMuscleGroupsIsNull() {
        validRequest.setMuscleGroups(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenNameIsBlank() {
        validRequest.setName("   ");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenCategoryIsBlank() {
        validRequest.setName("Valid");
        validRequest.setCategory("   ");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenNameAndCategoryNull() {
        validRequest.setName(null);
        validRequest.setCategory(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldCreateExerciseWhenValid() {
        when(exerciseRepository.findByName("Push Up")).thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise1.class))).thenAnswer(invocation -> {
            Exercise1 arg = invocation.getArgument(0);
            arg.setId(2L);
            return arg;
        });

        Exercise1 created = exerciseService.createExercise(validRequest);

        assertNotNull(created);
        assertEquals(2L, created.getId());
        assertEquals("Push Up", created.getName());
        verify(exerciseRepository).findByName("Push Up");
        verify(exerciseRepository).save(any(Exercise1.class));
    }

    @Test
    void shouldThrowConflictWhenCreatingWithExistingName() {
        when(exerciseRepository.findByName("Push Up")).thenReturn(Optional.of(exercise));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.createExercise(validRequest)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // ---------- updateExercise ----------

    @Test
    void shouldUpdateExerciseWhenValid() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.findByNameAndIdNot("Push Up Updated", 1L)).thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise1.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExerciseRequest updateRequest = new ExerciseRequest("Push Up Updated", "Strength", Set.of("Chest"), "Updated");

        Exercise1 updated = exerciseService.updateExercise(1L, updateRequest);

        assertEquals("Push Up Updated", updated.getName());
        assertEquals("Updated", updated.getDescription());
        verify(exerciseRepository).findById(1L);
        verify(exerciseRepository).findByNameAndIdNot("Push Up Updated", 1L);
        verify(exerciseRepository).save(any(Exercise1.class));
    }

    @Test
    void shouldThrowConflictWhenUpdatingToExistingName() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        Exercise1 other = Exercise1.builder().id(2L).name("Push Up Updated").build();
        when(exerciseRepository.findByNameAndIdNot("Push Up Updated", 1L)).thenReturn(Optional.of(other));

        ExerciseRequest updateRequest = new ExerciseRequest("Push Up Updated", "Strength", Set.of("Chest"), "Updated");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.updateExercise(1L, updateRequest)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExisting() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        ExerciseRequest updateRequest = new ExerciseRequest("X", "Y", Set.of("A"), "D");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.updateExercise(99L, updateRequest)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenUpdateRequestHasNullNameOrCategory() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        ExerciseRequest updateRequest = new ExerciseRequest(null, "Strength", Set.of("Chest"), "Updated");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.updateExercise(1L, updateRequest)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void update_shouldThrowWhenCategoryBlank() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        ExerciseRequest updateRequest = new ExerciseRequest("Name", "   ", Set.of("A"), "D");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.updateExercise(1L, updateRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void update_shouldThrowWhenMuscleGroupsEmpty() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        ExerciseRequest updateRequest = new ExerciseRequest("Name", "Cat", Set.of(), "D");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.updateExercise(1L, updateRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // ---------- deleteExercise ----------

    @Test
    void shouldDeleteExerciseSuccessfully() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        exerciseService.deleteExercise(1L);

        verify(exerciseRepository).delete(exercise);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExisting() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> exerciseService.deleteExercise(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}