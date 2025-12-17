package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_log_id")
    private SessionLog sessionLog;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_execution_template_id")
    private ExerciseExecutionTemplate exerciseTemplate;

    @Column(nullable = false)
    private Integer actualSets;

    @Column(nullable = false)
    private Integer actualReps;

    @Column(nullable = false)
    private Double actualWeight;

    @Column(nullable = false)
    private Boolean completed;

    @Column(length = 1000)
    private String notes;
}


