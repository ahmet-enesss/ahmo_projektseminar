package com.example.fitnessapp.DTOs;

import lombok.Data;

@Data
public class AddTemplateToPlanRequest {
    private Long templateId;
    private Integer position; // optional
}
