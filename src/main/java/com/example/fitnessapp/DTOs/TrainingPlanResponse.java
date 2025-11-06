package com.example.fitnessapp.DTOs;


import lombok.Data;

import java.util.List;

@Data//Lombok erzeugt autom. Getter,Setter,etc.
public class TrainingPlanResponse {
    private Long id; //ID der Trainingsplan
    private String name; //Name der Trainingsplan
    private String description; //Beschreibung der Trainingsplan
    private List<Long> sessionIds; //Liste der IDs aller Trainingseinheiten die zu diesem Plan gehören
}