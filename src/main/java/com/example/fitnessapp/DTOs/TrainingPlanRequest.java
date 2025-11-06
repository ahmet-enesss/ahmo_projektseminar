package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data //Lombok erstellt autom. Getter,Setter,etc.
public class TrainingPlanRequest {
    @NotBlank(message = "Name darf nicht leer sein") //Validierung: Feld darf nicht leer sein
    private String name;

    @NotBlank(message = "Beschreibung darf nicht leer sein")//Validierung: Feld darf nicht leer sein
    private String description;
}