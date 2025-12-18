package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.*;
import com.example.fitnessapp.Service.SessionLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // REST-Controller für das Logging und die Steuerung von Trainingseinheiten
@RequestMapping("/api/sessionlogs") // Basis-URL für alle Session-Log-Endpunkte
public class SessionLogController {

    private final SessionLogService service; // Service-Schicht mit der Geschäftslogik für Session-Logs

    public SessionLogController(SessionLogService service) {
        this.service = service;
    } // Konstruktor-Injection des Services

    @PostMapping("/start") // Startet eine neue Trainingseinheit (Session-Log)
    public SessionLogDetailResponse start(@Valid @RequestBody SessionLogCreateRequest request) { // Validierter Request zum Starten einer Session
        return service.start(request);   // Delegiert den Start der Session an den Service
    }

    @GetMapping("/{id}")  // Liefert Detailinformationen zu einer laufenden oder abgeschlossenen Session
    public SessionLogDetailResponse get(@PathVariable Long id) { // ID der Session aus der URL
        return service.getDetail(id); // Ruft die Session-Details über den Service ab
    }

    @PutMapping("/execution") // Aktualisiert die Ausführung eines einzelnen Exercises innerhalb der Session
    public ExecutionLogResponse updateExecution(@Valid @RequestBody ExecutionLogUpdateRequest request) { // Validierter Request zur Aktualisierung eines Execution-Logs
        return service.updateExecution(request); // Übergibt das Update an den Service
    }

    @PostMapping("/{id}/complete") // Markiert eine Session als abgeschlossen und berechnet die Zusammenfassung
    public SessionLogSummaryResponse complete(@PathVariable Long id) { // ID der abzuschließenden Session
        return service.complete(id); // Schließt die Session über den Service ab
    }

    @DeleteMapping("/{id}") // Bricht eine laufende Session ab und verwirft die bisherigen Logs
    public ResponseEntity<Void> abort(@PathVariable Long id) {  // ID der abzubrechenden Session
        service.abort(id);  // Abbruchlogik im Service
        return ResponseEntity.noContent().build();  // HTTP 204 No Content als Bestätigung
    }
}


