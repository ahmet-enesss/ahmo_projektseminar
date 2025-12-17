package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.TrainingSessionTemplateOverviewResponse;
import com.example.fitnessapp.DTOs.TrainingSessionTemplateRequest;
import com.example.fitnessapp.Service.TrainingSessionTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session-templates")
public class TrainingSessionTemplateController {

    private final TrainingSessionTemplateService service;

    public TrainingSessionTemplateController(TrainingSessionTemplateService service) {
        this.service = service;
    }

    @GetMapping
    public List<TrainingSessionTemplateOverviewResponse> getAllSessions() {
        return service.getAllSessions();
    }

    @GetMapping("/{id}")
    public TrainingSessionTemplateOverviewResponse getSessionById(@PathVariable Long id) {
        return service.getSessionById(id);
    }

    @PostMapping
    public ResponseEntity<TrainingSessionTemplateOverviewResponse> createSession(
            @Valid @RequestBody TrainingSessionTemplateRequest request) {
        TrainingSessionTemplateOverviewResponse created = service.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TrainingSessionTemplateOverviewResponse updateSession(
            @PathVariable Long id,
            @Valid @RequestBody TrainingSessionTemplateRequest request) {
        return service.updateSession(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        service.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}

