package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.TrainingPlanRequest;
import com.example.fitnessapp.DTOs.TrainingPlanDetailResponse;
import com.example.fitnessapp.Service.TrainingPlanService1;
import com.example.fitnessapp.DTOs.TrainingPlanOverviewResponse;
import com.example.fitnessapp.Model.TrainingPlan1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController //markiert diese Klasse als Rest-Controller
@RequestMapping("/api/trainingplans")// Basis-URL für alle Trainingsplänerouten
public class TrainingPlanController1 {

    @Autowired
    private TrainingPlanService1 trainingPlanService; //Zugriff auf Service-Klasse

    @GetMapping //Methode die gibt alle Trainingspläne als Liste zurück
    public List<TrainingPlanOverviewResponse> getAllTrainingPlans() {
        return trainingPlanService.getAllTrainingPlans();
    }

    @GetMapping("/{id}") //Methode die gibt einen Trainingsplan nach seiner ID zurück
    public ResponseEntity<TrainingPlanDetailResponse> getTrainingPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingPlanService.getTrainingPlanById(id));
    }

    @PostMapping // Methode die einen neuen Trainingsplan erstellt
    public ResponseEntity<?> createTrainingPlan(
            @Valid @RequestBody TrainingPlanRequest request,//validiert eingehende Daten
            BindingResult bindingResult) {
        ResponseEntity<?> validationResponse = buildValidationResponse(bindingResult);
        if (validationResponse != null) {
            return validationResponse;
        }//Erstellung des Plans über den Builder
        TrainingPlan1 plan = TrainingPlan1.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        //speichert des neuen Plans über den Service
        TrainingPlan1 created = trainingPlanService.createTrainingPlan(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrainingPlan(@PathVariable Long id,
                                                @Valid @RequestBody TrainingPlanRequest request,
                                                BindingResult bindingResult) {
        ResponseEntity<?> validationResponse = buildValidationResponse(bindingResult);
        if (validationResponse != null) {
            return validationResponse;
        }
        TrainingPlan1 updated = trainingPlanService.updateTrainingPlan(id, request);
        return ResponseEntity.ok(Map.of(
                "message", "Trainingsplan erfolgreich gespeichert",
                "plan", updated
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingPlan(@PathVariable Long id) {
        trainingPlanService.deleteTrainingPlan(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> buildValidationResponse(BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            return null;
        }
        String errorMsg = bindingResult.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", errorMsg
                ));
    }
}