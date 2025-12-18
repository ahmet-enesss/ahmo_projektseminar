package com.example.fitnessapp.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseExecutionTemplateResponse { // Response-DTO zur Darstellung eines Exercise-Execution-Templates
    private Long id; // Eindeutige ID des Templates
    private Long sessionId; // ID der zugehörigen Trainingseinheit
    private Long exerciseId;  // ID der referenzierten Übung
    private String exerciseName; // Anzeigename der Übung
    private String exerciseCategory; // Kategorie der Übung (z. B. "Push", "Pull", "Beine")
    private Integer plannedSets; // Geplante Anzahl der Sätze
    private Integer plannedReps;  // Geplante Wiederholungen pro Satz
    private Double plannedWeight; // Geplantes Gewicht (z. B. in kg)
    private Integer orderIndex;  // Reihenfolge der Übung innerhalb der Session
}


