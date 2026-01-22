package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data // Lombok: erzeugt Getter, Setter, equals, hashCode und toString
public class ExecutionLogUpdateRequest {

    @NotNull
    private Long executionLogId;
    // ID des zu aktualisierenden Execution-Logs (Pflichtfeld)
    // Wenn negativ oder nicht vorhanden, wird ein neuer Eintrag erstellt

    @NotNull
    @Positive
    private Integer actualSets; // Tatsächlich absolvierte Anzahl der Sätze (muss > 0 sein)

    @NotNull
    @Positive
    private Integer actualReps;  // Tatsächlich absolvierte Wiederholungen (muss > 0 sein)

    @NotNull
    @PositiveOrZero
    private Double actualWeight; // Tatsächlich verwendetes Gewicht (0 erlaubt, z. B. Körpergewicht)

    private Boolean completed; // Optional: Kennzeichnet, ob die Übung abgeschlossen ist

    private String notes; // Optional: Freitext-Notizen zur Übungsausführung

    // Optionale Felder für die Erstellung eines neuen ExecutionLogs
    private Long sessionLogId; // Wird benötigt, wenn executionLogId nicht existiert
    private Long exerciseTemplateId; // Wird benötigt, wenn executionLogId nicht existiert
}



