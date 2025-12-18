package com.example.fitnessapp.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok: erzeugt Getter, Setter, equals, hashCode und toString
@Builder // Lombok: ermöglicht das Erstellen des Objekts per Builder-Pattern
@NoArgsConstructor // Lombok: erzeugt einen parameterlosen Konstruktor
@AllArgsConstructor // Lombok: erzeugt einen Konstruktor mit allen Feldern
public class ExecutionLogResponse {
    private Long id; // Eindeutige ID des Execution-Logs
    private Long exerciseTemplateId; // Referenz auf das zugehörige Exercise-Template
    private String exerciseName; // Anzeigename der Übung (z. B. "Bankdrücken")
    private Integer plannedSets; // Geplante Anzahl der Sätze
    private Integer plannedReps; // Geplante Wiederholungen pro Satz
    private Double plannedWeight; // Geplantes Gewicht (z. B. in kg)
    private Integer actualSets; // Tatsächlich absolvierte Anzahl der Sätze
    private Integer actualReps;   // Tatsächlich absolvierte Wiederholungen
    private Double actualWeight;  // Tatsächlich verwendetes Gewicht
    private Boolean completed;  // Gibt an, ob die Übung erfolgreich abgeschlossen wurde
    private String notes; // Optionale Notizen zur Ausführung (z. B. Technik, Schmerzen)
}


