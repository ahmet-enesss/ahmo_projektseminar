package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_session_id")
    private TrainingSession1 templateSession;
    // Startzeit
    private LocalDateTime startTime;
    // Endzeit
    private LocalDateTime endTime;
    // Aktuelle Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogStatus status;
    // Notizen die sich auf die Situng beziehen
    @Column(length = 2000)
    private String notes;
    // Eine Sammlung der Übungsprotokolle
    @OneToMany(mappedBy = "sessionLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ExecutionLog> exerciseLogs = new HashSet<>();
}


