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

@SuppressWarnings("unused")
@Service // Markiert die Klasse als Service-Komponente
public class ExerciseService1 {

    @Autowired   // Spring injiziert automatisch das passende Repository
    private ExerciseRepository1 exerciseRepository;
    // Gibt alle Übungen aus der Datenbank zurück
    public List<Exercise1> getAllExercises() {
        return exerciseRepository.findAll();
    }
    // Holt eine Übung anhand ihrer ID aus der Datenbank
    // Wenn keine Übung mit dieser ID existiert, wird ein 404-Fehler (NOT_FOUND) ausgelöst
    public Exercise1 getExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
    }



    @Transactional  // Stellt sicher, dass alle Datenbankoperationen in einer Transaktion ausgeführt werden
    public Exercise1 createExercise(ExerciseRequest request) {
        // Prüft, ob bereits eine Übung mit diesem Namen existiert
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
        return exerciseRepository.save(exercise);  // Speichert die neue Übung in der Datenbank und gibt das gespeicherte Objekt zurück
    }

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

    @Transactional
    public void deleteExercise(Long id) {
        Exercise1 existing = getExerciseById(id);
        exerciseRepository.delete(existing);
    }

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