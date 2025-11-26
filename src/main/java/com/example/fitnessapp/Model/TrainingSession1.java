package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSession1 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Verbindung zu einem Trainingsplan
    @ManyToOne
    @JoinColumn(name = "training_plan_id")
    private TrainingPlan1 trainingPlan;

    //Name der Trainingssession
    @Column(nullable = false)
    private String name;

    //Datum an dem die Session geplant ist
    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingSessionStatus status;

    //Beziehung zu den Übungen: Eine Session kann mehrere Exercises (Übungen) erhalten
    @ManyToMany
    @JoinTable(
            name = "session_exercises",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "exercise_id")
    )
    private Set<Exercise1> exerciseExecutions;
}