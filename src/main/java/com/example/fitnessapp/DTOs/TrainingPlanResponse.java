package com.example.fitnessapp.DTOs;


import lombok.Data;

import java.util.List;

@Data
public class TrainingPlanResponse {
    private Long id;
    private String name;
    private String description;
    private List<Long> sessionIds;
}