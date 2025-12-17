package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ExerciseExecutionTemplateRequest {

    @NotNull
    private Long sessionId;

    @NotNull
    private Long exerciseId;

    @NotNull
    @Positive
    private Integer plannedSets;

    @NotNull
    @Positive
    private Integer plannedReps;

    @NotNull
    @PositiveOrZero
    private Double plannedWeight;

    @NotNull
    @Positive
    private Integer orderIndex;
}


