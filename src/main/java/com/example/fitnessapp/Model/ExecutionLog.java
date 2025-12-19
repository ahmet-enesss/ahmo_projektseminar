package com.example.fitnessapp.Model;

// diese Klasse repräsentiert die Protokollierung einer Übungsausführung in einer Trainingseinheit
// und speichert die Daten einer spezifischen Übungsausführung,wie die Anzahl der
// tatsächlichen Sets, Wiederholungen, Gewicht und den Status, ob sie abgeschlossen wurde
import jakarta.persistence.*;
import lombok.*;
@Entity //Kennzeichnet die Klasse als JPA-Entity
@Getter //Generiert automatisch Getter-Methoden
@Setter //Generiert automatisch Setter-Methoden
@NoArgsConstructor //Erstellt einen parameterlosen Konstruktor
@AllArgsConstructor // Erstellt einen Konstruktor mit allen Parametern
@Builder //Ermöglicht das Erstellen von Objekten mit dem Builder-Pattern
public class ExecutionLog {

    @Id // Markiert das Feld als Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Verweist auf das zugehörige SessionLog zu dem diese Übung gehört
    @ManyToOne(optional = false)
    @JoinColumn(name = "session_log_id")
    private SessionLog sessionLog;

    //Verweist auf die Vorlage der Übungsausführung (ExerciseExecutionTemplate) die diese spezifische Ausführung beschreibt
    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_execution_template_id")
    private ExerciseExecutionTemplate exerciseTemplate;

    //Die tatsächliche Anzahl durchgeführter Sets
    @Column(nullable = false)
    private Integer actualSets;

    //Die tatsächliche Anzahl durchgeführter Wiederholungen
    @Column(nullable = false)
    private Integer actualReps;

    //Das tatsächliche verwendete Gewicht bei der Übungsausführung
    @Column(nullable = false)
    private Double actualWeight;

    //Gibt an, ob die Übung abgeschlossen wurde wahr oder falsch
    @Column(nullable = false)
    private Boolean completed;

    //Zusätzliche Notizen zur Übungsausführung
    @Column(length = 1000)
    private String notes;
}


