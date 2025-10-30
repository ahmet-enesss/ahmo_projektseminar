package com.example.fitnessapp.Excercise;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
class ExerciseController {

    private final ExerciseService service;

    public ExerciseController(ExerciseService service) {
        this.service = service;
    }

    @GetMapping
    public List<Exercise> getAllExercises() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exercise> getExerciseById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createExercise(@RequestBody Exercise exercise) {
        if (exercise.getName() == null || exercise.getCategory() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Name and category are required.");
        }
        try {
            Exercise newExercise = service.create(exercise);
            return ResponseEntity.status(HttpStatus.CREATED).body(newExercise);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
