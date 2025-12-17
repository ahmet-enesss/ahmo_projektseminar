package com.example.fitnessapp.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionTemplateOverviewResponse {
    private Long id;
    private String name;
    private Long planId;
    private String planName;
    private Integer orderIndex;
    private Integer exerciseCount;
    private Long executionCount; // Wie oft wurde diese Session bereits durchgeführt
}

