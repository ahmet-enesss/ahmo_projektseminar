package com.example.fitnessapp.uebung;

import com.example.fitnessapp.uebung.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    Optional<Exercise> findByNameIgnoreCase(String name);
}
