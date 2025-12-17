package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.ExerciseExecutionTemplateRequest;
import com.example.fitnessapp.DTOs.ExerciseExecutionTemplateResponse;
import com.example.fitnessapp.Service.ExerciseExecutionTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainingsessions/{sessionId}/exercise-templates")
public class ExerciseExecutionTemplateController {

    private final ExerciseExecutionTemplateService service;

    public ExerciseExecutionTemplateController(ExerciseExecutionTemplateService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExerciseExecutionTemplateResponse> list(@PathVariable Long sessionId) {
        return service.getForSession(sessionId);
    }

    @PostMapping
    public ResponseEntity<ExerciseExecutionTemplateResponse> create(@PathVariable Long sessionId,
                                                                    @Valid @RequestBody ExerciseExecutionTemplateRequest request) {
        request.setSessionId(sessionId);
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ExerciseExecutionTemplateResponse update(@PathVariable Long sessionId,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody ExerciseExecutionTemplateRequest request) {
        request.setSessionId(sessionId);
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


