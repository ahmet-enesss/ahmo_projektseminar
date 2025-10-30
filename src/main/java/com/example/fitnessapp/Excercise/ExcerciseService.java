package com.example.fitnessapp.Excercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExerciseService {
    private final ExerciseRepository repository;

    public ExerciseService(ExerciseRepository repository) {
        this.repository = repository;
    }

    public List<Exercise> getAll() {
        return repository.repository();
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