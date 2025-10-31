package com.example.fitnessapp.Service;


import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new RuntimeException("Exercise not found"));
    }

    @Transactional
    public Exercise1 createExercise(Exercise1 exercise) {
        if (exerciseRepository.findByName(exercise.getName()).isPresent()) {
            throw new RuntimeException("Exercise with this name already exists");
        }
        return exerciseRepository.save(exercise);
    }
}