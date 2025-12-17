package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseExecutionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_session_id")
    private TrainingSession1 trainingSession;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_id")
    private Exercise1 exercise;

    @Column(nullable = false)
    private Integer plannedSets;

    @Column(nullable = false)
    private Integer plannedReps;

    @Column(nullable = false)
    private Double plannedWeight;

    @Column(nullable = false)
    private Integer orderIndex;
}


