package com.example.fitnessapp.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingPlanOverviewResponse {
    private Long id;
    private String name;
    private String description;
    private long sessionCount;
}



