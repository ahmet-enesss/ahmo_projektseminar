package com.example.fitnessapp.Model;
//Diese Klasse repräsentiert eine Sitzungsaufzeichnung einer Trainingssitzung
//einschließlich der Dauer, des Status und der zugehörigen Übungsprotokolle

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity//Kennzeichnet die Klasse als JPA-Entity
@Getter //Generiert automatisch Getter-Methoden
@Setter //Generiert automatisch Setter-Methoden
@NoArgsConstructor //Erstellt einen parameterlosen Konstruktor
@AllArgsConstructor // Erstellt einen Konstruktor mit allen Parametern
@Builder //Ermöglicht das Erstellen von Objekten mit dem Builder-Pattern
public class SessionLog {

    @Id // Markiert das Feld als Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Verweist auf die Vorlage für diese Sitzung (TrainingSession1)
    @ManyToOne(optional = false)
    @JoinColumn(name = "training_session_id")
    private TrainingSession1 templateSession;

    //Startzeit der Sitzung
    private LocalDateTime startTime;

    // Endzeit der Sitzung
    private LocalDateTime endTime;

    //Der aktuelle Status der Sitzung definiert durch das Enum LogStatus
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogStatus status;

    //Notizen die sich auf die gesamte Sitzung beziehen
    @Column(length = 2000)
    private String notes;

    //Eine Sammlung der Übungsprotokolle (ExecutionLogs) die zu dieser Sitzung gehören
    @OneToMany(mappedBy = "sessionLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ExecutionLog> exerciseLogs = new HashSet<>();
}


