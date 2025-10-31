package com.example.fitnessapp.DTOs;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class TrainingSessionRequest {
    private Long planId;
    private String name;
    private LocalDate scheduledDate;
    private Set<Long> exerciseIds;
}