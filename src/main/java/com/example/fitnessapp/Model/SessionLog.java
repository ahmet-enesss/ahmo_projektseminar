package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity // JPA-Entity für die Protokollierung einer Trainingssession-Ausführung
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primärschlüssel der Entity, automatisch generiert

    @ManyToOne(optional = false) // Many-to-One-Beziehung zum Template der Trainingssession
    @JoinColumn(name = "training_session_id")
    private TrainingSession1 templateSession;

    private LocalDateTime startTime; // Zeitpunkt des Starts der Session

    private LocalDateTime endTime; // Zeitpunkt des Endes der Session (null bei laufender Session)

    @Enumerated(EnumType.STRING) // Status der Session (z. B. RUNNING, COMPLETED, ABORTED)
    @Column(nullable = false)
    private LogStatus status;

    @Column(length = 2000)   // Optional: Notizen zur gesamten Session (max. 2000 Zeichen)
    private String notes;

    // One-to-Many-Beziehung zu den Exercise Execution Logs dieser Session
    // Cascade ALL: Änderungen an SessionLog werden auf ExerciseLogs übertragen
    // orphanRemoval: Entfernte ExerciseLogs werden automatisch gelöscht
    @OneToMany(mappedBy = "sessionLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ExecutionLog> exerciseLogs = new HashSet<>();
}


