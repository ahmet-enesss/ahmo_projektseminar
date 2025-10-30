package com.example.fitnessapp.Excercise;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;

    @ElementCollection
    private List<String> muscleGroups;

    @Column(length = 500)
    private String description;

    // --- Konstruktoren ---
    public Exercise() {}

    public Exercise(String name, String category, List<String> muscleGroups, String description) {
        this.name = name;
        this.category = category;
        this.muscleGroups = muscleGroups;
        this.description = description;
    }

    // --- Getter und Setter ---
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public List<String> getMuscleGroups() { return muscleGroups; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setMuscleGroups(List<String> muscleGroups) { this.muscleGroups = muscleGroups; }
    public void setDescription(String description) { this.description = description; }
}