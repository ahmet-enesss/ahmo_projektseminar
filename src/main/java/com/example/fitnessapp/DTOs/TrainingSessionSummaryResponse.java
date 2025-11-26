package com.example.fitnessapp.DTOs;

import com.example.fitnessapp.Model.TrainingSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionSummaryResponse {
    private Long id;
    private String name;
    private LocalDate scheduledDate;
    private int exerciseCount;
    private TrainingSessionStatus status;
}



