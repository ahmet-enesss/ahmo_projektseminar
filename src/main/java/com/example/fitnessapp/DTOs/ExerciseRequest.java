package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class ExerciseRequest {

    @NotBlank(message = "Name darf nicht leer sein")
    private String name;

    @NotBlank(message = "Kategorie darf nicht leer sein")
    private String category;

    @NotEmpty(message = "Mindestens eine Muskelgruppe ist erforderlich")
    private Set<@NotBlank(message = "Einträge in Muskelgruppen dürfen nicht leer sein") String> muscleGroups;

    private String description;

    public ExerciseRequest(String name, String category, Set<String> muscleGroups, String description) {
        this.name = name;
        this.category = category;
        this.muscleGroups = muscleGroups;
        this.description = description;
    }

}

