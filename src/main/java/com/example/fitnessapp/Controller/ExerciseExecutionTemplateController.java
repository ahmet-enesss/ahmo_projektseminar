package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.ExerciseExecutionTemplateRequest;
import com.example.fitnessapp.DTOs.ExerciseExecutionTemplateResponse;
import com.example.fitnessapp.Service.ExerciseExecutionTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Kennzeichnet die Klasse als REST-Controller (JSON-basierte HTTP-API)
@RequestMapping("/api/trainingsessions/{sessionId}/exercise-templates")
public class ExerciseExecutionTemplateController {

    private final ExerciseExecutionTemplateService service; // Service-Schicht, die die Geschäftslogik kapselt

    public ExerciseExecutionTemplateController(ExerciseExecutionTemplateService service) {
        this.service = service;
    } // Konstruktor-Injection des Services (empfohlen für Testbarkeit)

    @GetMapping  // GET-Endpunkt zum Abrufen aller Exercise-Templates einer Trainingseinheit
    public List<ExerciseExecutionTemplateResponse> list(@PathVariable Long sessionId) {
        return service.getForSession(sessionId);  // Delegiert den Abruf an den Service
    }

    @PostMapping // POST-Endpunkt zum Erstellen eines neuen Exercise-Templates
    public ResponseEntity<ExerciseExecutionTemplateResponse> create(@PathVariable Long sessionId,  // sessionId aus der URL
                                                                    @Valid @RequestBody ExerciseExecutionTemplateRequest request) { // Request-Body wird validiert (@Valid)
        request.setSessionId(sessionId); // Setzt die sessionId aus der URL in das Request-Objekt
        return ResponseEntity.ok(service.create(request));  // Erstellt das Template und gibt HTTP 200 mit Response-Body zurück
    }

    @PutMapping("/{id}") // PUT-Endpunkt zum Aktualisieren eines bestehenden Exercise-Templates
    public ExerciseExecutionTemplateResponse update(@PathVariable Long sessionId, // ID des zu aktualisierenden Templates
                                                    @PathVariable Long id, // Validierter Request-Body
                                                    @Valid @RequestBody ExerciseExecutionTemplateRequest request) {
        request.setSessionId(sessionId);  // Verknüpft das Update mit der angegebenen Trainingseinheit
        return service.update(id, request);  // Aktualisiert das Template über den Service
    }

    @DeleteMapping("/{id}")   // DELETE-Endpunkt zum Löschen eines Exercise-Templates
    public ResponseEntity<Void> delete(@PathVariable Long id) { // Löscht das Template anhand der ID
        service.delete(id);
        return ResponseEntity.noContent().build();  // Gibt HTTP 204 No Content zurück (Standard für erfolgreiche Deletes)
    }
}


