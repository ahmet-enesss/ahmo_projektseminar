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

    private final TrainingSessionTemplateService service; // Service-Schicht mit der Geschäftslogik für Trainingssession-Templates

    public TrainingSessionTemplateController(TrainingSessionTemplateService service) {
        this.service = service;
    } // Konstruktor-Injection des Services

    @GetMapping // Liefert eine Übersicht aller vorhandenen Trainingssession-Templates
    public List<TrainingSessionTemplateOverviewResponse> getAllSessions() {
        return service.getAllSessions();
    }  // Delegiert den Abruf an den Service

    @GetMapping("/{id}") // Liefert ein einzelnes Trainingssession-Template anhand der ID
    public TrainingSessionTemplateOverviewResponse getSessionById(@PathVariable Long id) { // ID des Templates aus der URL
        return service.getSessionById(id); // Holt das Template über den Service
    }

    @PostMapping // Erstellt ein neues Trainingssession-Template
    public ResponseEntity<TrainingSessionTemplateOverviewResponse> createSession(
            @Valid @RequestBody TrainingSessionTemplateRequest request) { // Validierter Request-Body mit Template-Daten
        TrainingSessionTemplateOverviewResponse created = service.createSession(request);
        // Erstellt das Template im Service
        return ResponseEntity.status(HttpStatus.CREATED).body(created); // Gibt HTTP 201 Created mit dem erstellten Objekt zurück
    }

    @PutMapping("/{id}")  // Aktualisiert ein bestehendes Trainingssession-Template
    public TrainingSessionTemplateOverviewResponse updateSession(
            @PathVariable Long id,  // ID des zu aktualisierenden Templates
            @Valid @RequestBody TrainingSessionTemplateRequest request) { // Validierter Request-Body mit neuen Daten
        return service.updateSession(id, request);   // Übergibt das Update an den Service
    }

    @DeleteMapping("/{id}")   // Löscht ein Trainingssession-Template
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) { // ID des zu löschenden Templates
        service.deleteSession(id); // Löscht das Template über den Service
        return ResponseEntity.noContent().build(); // HTTP 204 No Content als Bestätigung
    }
}

