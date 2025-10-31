package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.Exercise1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExerciseRepository1 extends JpaRepository<Exercise1, Long> {
    Optional<Exercise1> findByName(String name);
}