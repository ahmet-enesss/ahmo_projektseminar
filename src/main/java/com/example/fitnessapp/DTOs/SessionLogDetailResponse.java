package com.example.fitnessapp.DTOs;

import com.example.fitnessapp.Model.LogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionLogDetailResponse {
    private Long id;
    private Long sessionTemplateId;
    private String sessionName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LogStatus status;
    private String notes;
    private List<ExecutionLogResponse> executions;
}


