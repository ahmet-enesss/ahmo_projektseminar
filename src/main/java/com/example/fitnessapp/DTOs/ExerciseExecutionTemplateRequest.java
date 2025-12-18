package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ExerciseExecutionTemplateRequest { // Request-DTO zum Erstellen oder Aktualisieren eines Exercise-Execution-Templates

    @NotNull
    private Long sessionId;  // ID der zugehörigen Trainingseinheit (wird meist aus der URL gesetzt)

    @NotNull
    private Long exerciseId;    // ID der Übung, auf die sich dieses Template bezieht

    @NotNull
    @Positive
    private Integer plannedSets; // Geplante Anzahl der Sätze (muss > 0 sein)

    @NotNull
    @Positive
    private Integer plannedReps;  // Geplante Wiederholungen pro Satz (muss > 0 sein)

    @NotNull
    @PositiveOrZero
    private Double plannedWeight;  // Geplantes Gewicht (0 erlaubt, z. B. Körpergewichtsübungen)

    @NotNull
    @Positive
    private Integer orderIndex; // Reihenfolge der Übung innerhalb der Session (Start bei 1 empfohlen)
}


