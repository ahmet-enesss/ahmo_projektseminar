package com.example.fitnessapp.Model;
//Diese Klasse repräsentiert die geplanten Details einer Übung während einer Trainingseinheit
//und dient als Vorlage für die tatsächliche Ausführung der Übung

import jakarta.persistence.*;
import lombok.*;

@Entity//Kennzeichnet die Klasse als JPA-Entity
@Getter //Generiert automatisch Getter-Methoden
@Setter //Generiert automatisch Setter-Methoden
@NoArgsConstructor //Erstellt einen parameterlosen Konstruktor
@AllArgsConstructor // Erstellt einen Konstruktor mit allen Parametern
@Builder //Ermöglicht das Erstellen von Objekten mit dem Builder-Pattern
public class ExerciseExecutionTemplate {

    @Id // Markiert das Feld als Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Verweist auf die zugehörige Trainingssitzung (TrainingSession1) in der diese Übung eingeplant ist
    @ManyToOne(optional = false)
    @JoinColumn(name = "training_session_id")
    private TrainingSession1 trainingSession;

   //Verweist auf die spezifische Übung (Exercise1) die geplant ist
    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_id")
    private Exercise1 exercise;

    //Die geplante Anzahl an Sets für die Übung
    @Column(nullable = false)
    private Integer plannedSets;

    //Die geplante Anzahl an Wiederholungen pro Set
    @Column(nullable = false)
    private Integer plannedReps;

    //Das geplante Gewicht das bei der Übung verwendet werden soll
    @Column(nullable = false)
    private Double plannedWeight;

    //Die Reihenfolge in der die Übung in der Sitzung durchgeführt werden soll
    @Column(nullable = false)
    private Integer orderIndex;
}


