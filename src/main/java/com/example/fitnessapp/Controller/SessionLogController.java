package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.*;
import com.example.fitnessapp.Service.SessionLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessionlogs")
public class SessionLogController {

    private final SessionLogService service;

    public SessionLogController(SessionLogService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public SessionLogDetailResponse start(@Valid @RequestBody SessionLogCreateRequest request) {
        return service.start(request);
    }

    @GetMapping("/{id}")
    public SessionLogDetailResponse get(@PathVariable Long id) {
        return service.getDetail(id);
    }

    @PutMapping("/execution")
    public ExecutionLogResponse updateExecution(@Valid @RequestBody ExecutionLogUpdateRequest request) {
        return service.updateExecution(request);
    }

    @PostMapping("/{id}/complete")
    public SessionLogSummaryResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> abort(@PathVariable Long id) {
        service.abort(id);
        return ResponseEntity.noContent().build();
    }
}


