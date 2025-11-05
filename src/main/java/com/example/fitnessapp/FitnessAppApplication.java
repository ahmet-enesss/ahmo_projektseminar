package com.example.fitnessapp;

import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.HashSet;

@SpringBootApplication
public class FitnessAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessAppApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ExerciseRepository1 exerciseRepo, TrainingPlanRepository1 planRepo) {
        return args -> {
            if (exerciseRepo.findByName("Bankdrücken").isEmpty()) {
                exerciseRepo.save(Exercise1.builder()
                        .name("Bankdrücken")
                        .category("Freihantel")
                        .muscleGroups(new HashSet<>(Arrays.asList("Brust", "Trizeps", "Schulter")))
                        .description("Drücken der Langhantel von der Brust [...]")
                        .build());
            }

            if (planRepo.findByName("Push Day").isEmpty()) {
                planRepo.save(TrainingPlan1.builder()
                        .name("Push Day")
                        .description("Trainingsplan für Brust, Schulter und Trizeps.")
                        .build());
            }
        };
    }
}
