package com.example.fitnessapp;

import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class FitnessAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessAppApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ExerciseRepository1 repo) {
        return args -> {
            if (repo.findByName("Bankdrücken").isEmpty()) {
                repo.save(Exercise1.builder()
                        .name("Bankdrücken")
                        .category("Freihantel")
                        .muscleGroups(Collections.singleton("Brust, Trizeps, Schulter"))
                        .description("Drücken der Langhantel von der Brust [...]")
                        .build());
            }
        };
    }
}
