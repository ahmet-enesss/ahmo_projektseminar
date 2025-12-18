package com.example.fitnessapp.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: erzeugt Getter, Setter, equals, hashCode und toString
@Builder // Lombok: ermöglicht das Erstellen des Objekts per Builder-Pattern
@NoArgsConstructor // Lombok: parameterloser Konstruktor
@AllArgsConstructor // Lombok: Konstruktor mit allen Feldern
public class TrainingSessionTemplateOverviewResponse {
    private Long id; // Eindeutige ID des Trainingssession-Templates
    private String name; // Name der Trainingssession
    private Long planId; // ID des Trainingsplans, zu dem dieses Template gehört
    private String planName; // Name des Trainingsplans
    private Integer orderIndex; // Reihenfolge der Session innerhalb des Trainingsplans (niedriger Wert = frühere Ausführung)
    private Integer exerciseCount;
    // Anzahl der Übungen, die diese Session enthält
    private Long executionCount; // Wie oft wurde diese Session bereits durchgeführt
}

