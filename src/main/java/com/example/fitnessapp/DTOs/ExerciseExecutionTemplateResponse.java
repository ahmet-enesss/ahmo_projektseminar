package com.example.fitnessapp.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseExecutionTemplateResponse {
    private Long id;
    private Long sessionId;
    private Long exerciseId;
    private String exerciseName;
    private String exerciseCategory;
    private Integer plannedSets;
    private Integer plannedReps;
    private Double plannedWeight;
    private Integer orderIndex;
}


