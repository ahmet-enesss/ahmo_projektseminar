package com.example.fitnessapp.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLogResponse {
    private Long id;
    private Long exerciseTemplateId;
    private String exerciseName;
    private Integer plannedSets;
    private Integer plannedReps;
    private Double plannedWeight;
    private Integer actualSets;
    private Integer actualReps;
    private Double actualWeight;
    private Boolean completed;
    private String notes;
}


