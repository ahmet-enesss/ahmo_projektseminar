package com.example.fitnessapp.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: erzeugt Getter, Setter, equals, hashCode und toString
@Builder // Lombok: ermöglicht das Erstellen des Objekts per Builder-Pattern
@NoArgsConstructor // Lombok: parameterloser Konstruktor
@AllArgsConstructor // Lombok: Konstruktor mit allen Feldern
public class TrainingSessionSummaryResponse {
    private Long id;  // Eindeutige ID der Trainingssession
    private String name; // Anzeigename der Trainingssession
    private Integer orderIndex; // Reihenfolge der Session innerhalb eines Trainingsplans (niedriger Wert = frühere Ausführung)
    private int exerciseCount;  // Anzahl der enthaltenen Übungen in dieser Session
}



