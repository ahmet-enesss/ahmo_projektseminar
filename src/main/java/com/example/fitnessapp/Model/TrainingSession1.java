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

    @ManyToOne
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan1 trainingPlan;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @ManyToMany
    @JoinTable(
            name = "session_exercises",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "exercise_id")
    )
    private Set<Exercise1> exerciseExecutions;
}