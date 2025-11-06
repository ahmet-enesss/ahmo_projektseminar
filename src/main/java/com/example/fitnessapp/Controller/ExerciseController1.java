package com.example.fitnessapp.Controller;


import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Service.ExerciseService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Markiert die Klasse als REST-Controller (stellt HTTP-Endpunkte bereit)
@RequestMapping("/api/exercises") // Basis-URL für alle Methoden in diesem Controller (z. B. /api/exercises)
public class ExerciseController1 {

    @Autowired
    private ExerciseService1 exerciseService1;

    @GetMapping // GET-Anfrage auf /api/exercises -> alle Übungen werden zurückgegeben
    public List<Exercise1> getAllExercises() {
        return exerciseService1.getAllExercises();
    }

    @GetMapping("/{id}") // GET-Anfrage auf /api/exercises/{id} -> gibt eine Übung mit passender ID zurück
    public ResponseEntity<Exercise1> getExerciseById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService1.getExerciseById(id));
    }

    @PostMapping // POST-Anfrage auf /api/exercises -> erstellt eine neue Übung
    public ResponseEntity<Exercise1> createExercise(@RequestBody Exercise1 exercise) {
        Exercise1 created = exerciseService1.createExercise(exercise);  // Gibt die neu erstellte Übung zurück mit HTTP-Status 201
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
