package com.example.fitnessapp.DTOs;

import com.example.fitnessapp.Model.LogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionLogSummaryResponse {
    private Long id;
    private Long sessionTemplateId;
    private String sessionName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LogStatus status;
}


