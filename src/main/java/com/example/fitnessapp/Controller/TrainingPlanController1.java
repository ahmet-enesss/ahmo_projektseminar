package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.TrainingPlanRequest;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Service.TrainingPlanService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController //markiert diese Klasse als Rest-Controller
@RequestMapping("/api/trainingplans")// Basis-URL für alle Trainingsplänerouten
public class TrainingPlanController1 {

    @Autowired
    private TrainingPlanService1 trainingPlanService; //Zugriff auf Service-Klasse

    @GetMapping //Methode die gibt alle Trainingspläne als Liste zurück
    public List<TrainingPlan1> getAllTrainingPlans() {
        return trainingPlanService.getAllTrainingPlans();
    }

    @GetMapping("/{id}") //Methode die gibt einen Trainingsplan nach seiner ID zurück
    public ResponseEntity<TrainingPlan1> getTrainingPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingPlanService.getTrainingPlanById(id));
    }

    @PostMapping // Methode die einen neuen Trainingsplan erstellt
    public ResponseEntity<?> createTrainingPlan(
            @Valid @RequestBody TrainingPlanRequest request,//validiert eingehende Daten
            BindingResult bindingResult) {
        //falls Validierungfehler vorhanden sind
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of(
                            "status", 400,
                            "error", "Bad Request",
                            "message", errorMsg
                    ));
        }//Erstellung des Plans über den Builder
        TrainingPlan1 plan = TrainingPlan1.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        //speichert des neuen Plans über den Service
        TrainingPlan1 created = trainingPlanService.createTrainingPlan(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}