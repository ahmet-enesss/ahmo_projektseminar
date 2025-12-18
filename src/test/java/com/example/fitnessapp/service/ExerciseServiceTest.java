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

    // ---------- updateExercise ----------

//helo

    // ---------- deleteExercise ----------

    @Test
    void shouldDeleteExerciseSuccessfully() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));

        exerciseService.deleteExercise(1L);

        verify(exerciseRepository).delete(exercise);
    }
}