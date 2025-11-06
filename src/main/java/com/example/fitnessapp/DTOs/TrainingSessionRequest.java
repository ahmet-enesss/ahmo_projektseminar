
package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

public class TrainingSessionRequest {

    //ID des Trainingsplanes, zu dem die Session gehört
    @NotNull
    private Long planId;

    //Name der Trainingssesion
    @NotBlank
    private String name;

    //Datum an dem das Training stattfindet
    @NotNull
    private LocalDate scheduledDate;

    //IDs der Übungen, die in der Session gemaxht werden
    private Set<Long> exerciseIds;

    //Getter und Setter (werden von Spring für JSON automatisch genutzt)
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public Set<Long> getExerciseIds() { return exerciseIds; }
    public void setExerciseIds(Set<Long> exerciseIds) { this.exerciseIds = exerciseIds; }
}
