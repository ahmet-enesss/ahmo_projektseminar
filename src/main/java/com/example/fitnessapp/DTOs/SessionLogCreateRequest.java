package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SessionLogCreateRequest {

    @NotNull
    private Long sessionTemplateId;

    private String notes;
}


