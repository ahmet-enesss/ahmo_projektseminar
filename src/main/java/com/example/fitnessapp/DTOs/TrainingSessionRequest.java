
package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

public class TrainingSessionRequest {

    @NotNull
    private Long planId;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate scheduledDate;

    private Set<Long> exerciseIds;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public Set<Long> getExerciseIds() { return exerciseIds; }
    public void setExerciseIds(Set<Long> exerciseIds) { this.exerciseIds = exerciseIds; }
}
