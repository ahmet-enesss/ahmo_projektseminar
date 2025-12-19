package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity//Kennzeichnet die Klasse als JPA-Entity
@Getter //Generiert automatisch Getter-Methoden
@Setter //Generiert automatisch Setter-Methoden
@NoArgsConstructor //Erstellt einen parameterlosen Konstruktor
@AllArgsConstructor // Erstellt einen Konstruktor mit allen Parametern
@Builder //Ermöglicht das Erstellen von Objekten mit dem Builder-Pattern
public class TrainingSession1 {
    @Id // Markiert das Feld als Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Verbindung zu einem Trainingsplan (optional, kann auch null sein)
    @ManyToOne
    @JoinColumn(name = "training_plan_id")
    private TrainingPlan1 trainingPlan;

    //Name der Trainingssession-Vorlage
    @Column(nullable = false, unique = true) // Neu: global eindeutiger Name
    private String name;

    //Reihenfolge innerhalb des Plans (1-30)
    @Column(nullable = false)
    private Integer orderIndex;

    //Beziehung zu den Übungs-Templates über ExerciseExecutionTemplate
    @OneToMany(mappedBy = "trainingSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExerciseExecutionTemplate> exerciseExecutions;
}