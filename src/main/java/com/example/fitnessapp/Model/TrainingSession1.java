package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

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

    //Verbindung zu einem Trainingsplan (optional, kann auch null sein)
    @ManyToOne
    @JoinColumn(name = "training_plan_id")
    private TrainingPlan1 trainingPlan;

    //Name der Trainingssession-Vorlage
    @Column(nullable = false)
    private String name;

    //Reihenfolge innerhalb des Plans (1-30)
    @Column(nullable = false)
    private Integer orderIndex;

    //Beziehung zu den Übungs-Templates über ExerciseExecutionTemplate
    @OneToMany(mappedBy = "trainingSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExerciseExecutionTemplate> exerciseExecutions;
}