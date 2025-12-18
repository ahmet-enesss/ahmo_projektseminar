package com.example.fitnessapp.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class) // Behandelt Validierungsfehler von @Valid Annotationen
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) { // Feldfehler sammeln: Feldname → Fehlermeldung
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                errors.put(fe.getField(), fe.getDefaultMessage())
        );
        Map<String, Object> body = new HashMap<>();  // Response-Body zusammenstellen
        body.put("type", "validation");  // Typ der Fehlermeldung
        body.put("errors", errors);  // Detailinformationen zu den Validierungsfehlern
        return ResponseEntity.badRequest().body(body);  // HTTP 400 Bad Request zurückgeben
    }

    @ExceptionHandler(ResponseStatusException.class) // Behandelt ResponseStatusException (z. B. NOT_FOUND, CONFLICT)
    public ResponseEntity<Object> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "business"); // Nachricht entweder aus Reason oder aus Standard-Message
        body.put("message", ex.getReason() != null ? ex.getReason() : ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(body); // HTTP-Statuscode aus der Exception verwenden
    }
}
