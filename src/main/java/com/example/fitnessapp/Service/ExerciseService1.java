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

    public List<Exercise1> getAllExercises() {   // Liefert alle Übungen
        return exerciseRepository.findAll();
    }

    public Exercise1 getExerciseById(Long id) {     // Liefert eine einzelne Übung anhand der ID
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
    }

    @Transactional  // Erstellt eine neue Übung
    public Exercise1 createExercise(ExerciseRequest request) { // Prüft, ob bereits eine Übung mit demselben Namen existiert
        if (exerciseRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise with this name already exists");
        }
        validateRequest(request);  // Validierung der Eingabedaten
        Exercise1 exercise = Exercise1.builder()
                .name(request.getName())
                .category(request.getCategory())
                .muscleGroups(request.getMuscleGroups())
                .description(request.getDescription())
                .build();
        return exerciseRepository.save(exercise);
    }

    @Transactional // Neues Exercise-Entity erstellen
    public Exercise1 updateExercise(Long id, ExerciseRequest request) {
        Exercise1 existing = getExerciseById(id);
        validateRequest(request);   // Validierung der Eingabedaten
        if (exerciseRepository.findByNameAndIdNot(request.getName(), id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise with this name already exists");  // Prüft, ob der neue Name bereits für eine andere Übung existiert
        }
        existing.setName(request.getName());  // Felder aktualisieren
        existing.setCategory(request.getCategory());
        existing.setMuscleGroups(request.getMuscleGroups());
        existing.setDescription(request.getDescription());
        return exerciseRepository.save(existing);   // Speichern und zurückgeben
    }

    @Transactional  // Aktualisiert eine bestehende Übung
    public void deleteExercise(Long id) {
        Exercise1 existing = getExerciseById(id);
        exerciseRepository.delete(existing);
    }

    private void validateRequest(ExerciseRequest request) {
        // Validiert die Eingabedaten für Create/Update
        if (request.getName() == null || request.getName().isBlank()
                || request.getCategory() == null || request.getCategory().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and category are required");
        }
        if (request.getMuscleGroups() == null || request.getMuscleGroups().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one muscle group is required");
        }
    }
}