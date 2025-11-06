package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity //Kennzeichnet die Klasse als JPA-Entity
@Getter //Generiert automatisch Getter-Methoden
@Setter //Generiert automatisch Setter-Methoden
@NoArgsConstructor //Erstellt einen parameterlosen Konstruktor
@AllArgsConstructor // Erstellt einen Konstruktor mit allen Parametern
@Builder //Ermöglicht das Erstellen von Objekten mit dem Builder-Pattern
public class Exercise1 {
    @Id // Markiert das Feld als Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)   // Name darf nicht doppelt vorkommen und nicht null sein
    private String name;

    @Column(nullable = false)    // Kategorie muss immer angegeben werden
    private String category;

    @ElementCollection
    @CollectionTable(name = "exercise_muscle_groups", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "muscle_group")  // Eine Liste von betroffenen Muskelgruppen (wird in separater Tabelle gespeichert)
    private Set<String> muscleGroups;

    private String description;
}
