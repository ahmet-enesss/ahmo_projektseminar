package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPlanSessionTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_plan_id")
    private TrainingPlan1 trainingPlan;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_session_id")
    private TrainingSession1 trainingSession; // Referenz auf die Session-Vorlage

    private Integer position; // optional: Reihenfolge innerhalb des Plans

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
