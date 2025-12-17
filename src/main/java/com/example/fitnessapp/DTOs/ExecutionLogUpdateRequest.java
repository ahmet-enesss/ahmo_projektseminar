package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ExecutionLogUpdateRequest {

    @NotNull
    private Long executionLogId;

    @NotNull
    @Positive
    private Integer actualSets;

    @NotNull
    @Positive
    private Integer actualReps;

    @NotNull
    @PositiveOrZero
    private Double actualWeight;

    private Boolean completed;

    private String notes;
}


