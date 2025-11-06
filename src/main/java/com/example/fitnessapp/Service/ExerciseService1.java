package com.example.fitnessapp.Service;


import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

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
    public Exercise1 createExercise(Exercise1 exercise) {
        // Prüft, ob bereits eine Übung mit diesem Namen existiert
        if (exerciseRepository.findByName(exercise.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise with this name already exists");
        }
        // Prüft, ob wichtige Felder (Name, Kategorie, Muskelgruppen) ausgefüllt sind
        if (exercise.getName() == null || exercise.getCategory() == null || exercise.getMuscleGroups() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }
        return exerciseRepository.save(exercise);  // Speichert die neue Übung in der Datenbank und gibt das gespeicherte Objekt zurück
    }
}