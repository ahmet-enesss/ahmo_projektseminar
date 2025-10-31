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
public class Exercise1 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @ElementCollection
    @CollectionTable(name = "exercise_muscle_groups", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "muscle_group")
    private Set<String> muscleGroups;

    private String description;
}
