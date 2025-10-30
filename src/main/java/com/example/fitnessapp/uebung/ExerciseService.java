package com.example.fitnessapp.uebung;

import com.example.fitnessapp.uebung.model.Exercise;
import com.example.fitnessapp.uebung.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository repository;

    public ExerciseService(ExerciseRepository repository) {
        this.repository = repository;
    }

    public List<Exercise> getAll() {
        return repository.findAll();
    }

    public Exercise getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));
    }

    public Exercise create(Exercise exercise) {
        repository.findByNameIgnoreCase(exercise.getName()).ifPresent(e -> {
            throw new RuntimeException("Exercise with this name already exists");
        });
        return repository.save(exercise);
    }
}
