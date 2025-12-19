package com.example.fitnessapp.Service;

import com.example.fitnessapp.DTOs.ExerciseRequest;
import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class ExerciseService1 {

    @Autowired
    private ExerciseRepository1 exerciseRepository;

    public List<Exercise1> getAllExercises() {
        return exerciseRepository.findAll();
    }

    public Exercise1 getExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
    }
    // Erstellt neue Übung
    @Transactional
    public Exercise1 createExercise(ExerciseRequest request) {
        if (exerciseRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise with this name already exists");
        }
        validateRequest(request);
        Exercise1 exercise = Exercise1.builder()
                .name(request.getName())
                .category(request.getCategory())
                .muscleGroups(request.getMuscleGroups())
                .description(request.getDescription())
                .build();
        return exerciseRepository.save(exercise);
    }
    // Aktualisiert bestehende Übung
    @Transactional
    public Exercise1 updateExercise(Long id, ExerciseRequest request) {
        Exercise1 existing = getExerciseById(id);
        validateRequest(request);
        if (exerciseRepository.findByNameAndIdNot(request.getName(), id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise with this name already exists");
        }
        existing.setName(request.getName());
        existing.setCategory(request.getCategory());
        existing.setMuscleGroups(request.getMuscleGroups());
        existing.setDescription(request.getDescription());
        return exerciseRepository.save(existing);
    }
    // Löscht Übung
    @Transactional
    public void deleteExercise(Long id) {
        Exercise1 existing = getExerciseById(id);
        exerciseRepository.delete(existing);
    }
    // Validiert die Eingabedaten
    private void validateRequest(ExerciseRequest request) {
        if (request.getName() == null || request.getName().isBlank()
                || request.getCategory() == null || request.getCategory().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and category are required");
        }
        if (request.getMuscleGroups() == null || request.getMuscleGroups().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one muscle group is required");
        }
    }
}