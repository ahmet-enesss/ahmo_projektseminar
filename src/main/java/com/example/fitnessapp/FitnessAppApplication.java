package com.example.fitnessapp;

import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Model.TrainingPlan1;
import com.example.fitnessapp.Model.User;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import com.example.fitnessapp.Repository.TrainingPlanRepository1;
import com.example.fitnessapp.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Wichtig
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Wichtig

import java.util.Arrays;
import java.util.HashSet;

@SpringBootApplication
public class FitnessAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessAppApplication.class, args);
    }

    // --- DIESE METHODE IST WICHTIG FÜR ANGULAR (CORS) ---
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Erlaubt alle Pfade
                        .allowedOrigins("http://localhost:4200") // Erlaubt Angular Frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
    // ----------------------------------------------------

    @Bean
    CommandLineRunner initDatabase(ExerciseRepository1 exerciseRepo, TrainingPlanRepository1 planRepo, 
                                   UserRepository userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            // Initialdaten: Bankdrücken
            if (exerciseRepo.findByName("Bankdrücken").isEmpty()) {
                exerciseRepo.save(Exercise1.builder()
                        .name("Bankdrücken")
                        .category("Freihantel")
                        .muscleGroups(new HashSet<>(Arrays.asList("Brust", "Trizeps", "Schulter")))
                        .description("Drücken der Langhantel von der Brust [...]")
                        .build());
            }

            // Initialdaten: Push Day Plan
            if (planRepo.findByName("Push Day").isEmpty()) {
                planRepo.save(TrainingPlan1.builder()
                        .name("Push Day")
                        .description("Trainingsplan für Brust, Schulter und Trizeps.")
                        .build());
            }

            // Vordefinierte Benutzer (Passwörter werden gehasht)
            if (userRepo.findByUsername("max").isEmpty()) {
                userRepo.save(User.builder()
                        .username("max")
                        .password(passwordEncoder.encode("passwort123"))
                        .build());
            }

            if (userRepo.findByUsername("anna").isEmpty()) {
                userRepo.save(User.builder()
                        .username("anna")
                        .password(passwordEncoder.encode("passwort456"))
                        .build());
            }
        };
    }
}