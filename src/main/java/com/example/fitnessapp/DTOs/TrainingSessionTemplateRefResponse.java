package com.example.fitnessapp.DTOs;

import lombok.Builder;
import lombok.Data;

/**
 * DEPRECATED: Wurde früher verwendet, um Template-Referenzen in einem Plan anzuzeigen.
 */
@Deprecated
@Data
@Builder
public class TrainingSessionTemplateRefResponse {
    private Long id;
    private String name;
    private Integer position;
}
