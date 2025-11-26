package com.example.fitnessapp.DTOs;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrainingPlanDetailResponse {
    private Long id;
    private String name;
    private String description;
    private List<TrainingSessionSummaryResponse> sessions;
    private boolean hasSessions;
    private String sessionsHint;
}



