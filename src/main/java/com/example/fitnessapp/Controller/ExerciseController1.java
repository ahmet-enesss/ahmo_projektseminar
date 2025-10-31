package com.example.fitnessapp.Controller;


import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Service.ExerciseService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController1 {

    @Autowired
    private ExerciseService1 exerciseService;

    @GetMapping
    public List<Exercise1> getAllExercises() {
        return exerciseService.getAllExercises();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exercise1> getExerciseById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(exerciseService.getExerciseById(id));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createExercise(@RequestBody Exercise1 exercise) {
        try {
            Exercise1 created = exerciseService.createExercise(exercise);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
