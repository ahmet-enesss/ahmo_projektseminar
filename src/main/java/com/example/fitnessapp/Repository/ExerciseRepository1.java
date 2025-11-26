package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.Exercise1;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
// Repository-Schnittstelle für die Entity "Exercise1"
// Ermöglicht Datenbankzugriffe (CRUD-Operationen) ohne eigene SQL-Abfragen
public interface ExerciseRepository1 extends JpaRepository<Exercise1, Long> {
    // Sucht eine Übung anhand ihres Namens
    // Gibt ein Optional zurück (kann also leer sein, wenn kein Eintrag gefunden
    Optional<Exercise1> findByName(String name);

    Optional<Exercise1> findByNameAndIdNot(String name, Long id);
}