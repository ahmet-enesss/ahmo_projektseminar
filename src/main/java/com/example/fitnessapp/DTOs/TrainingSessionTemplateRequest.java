package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class TrainingSessionTemplateRequest {

    //ID des Trainingsplanes (optional, kann null sein)
    private Long planId;

    //Name der Trainingssession-Vorlage
    @NotBlank(message = "Name ist erforderlich")
    private String name;

    //Reihenfolge im Plan (1-30)
    @NotNull(message = "Reihenfolge ist erforderlich")
    @Min(value = 1, message = "Reihenfolge muss mindestens 1 sein")
    @Max(value = 30, message = "Reihenfolge darf maximal 30 sein")
    private Integer orderIndex;

    //Getter und Setter
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}

