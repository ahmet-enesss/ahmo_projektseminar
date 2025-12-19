package com.example.fitnessapp.Model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@Entity //markiert die Klasse als JPA-Entität und somit in der Datenbank gespeichert
@Getter //Generiert automatisch Getter-Methoden
@Setter //Generiert automatisch Setter-Methoden
@NoArgsConstructor //Erstellt einen parameterlosen Konstruktor
@AllArgsConstructor // Erstellt einen Konstruktor mit allen Parametern
@Builder //Ermöglicht das Erstellen von Objekten mit dem Builder-Pattern
public class TrainingPlan1 {
    @Id // Markiert das Feld als Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY)//autom. erstellung von IDs durch die Datenbank
    private Long id;

    @Column(unique = true, nullable = false) //Name muss eindeutig und niht leer sein
    private String name;

    @Column(nullable = false) // Beschreibung darf nicht leer sein
    private String description;

    @OneToMany(mappedBy = "trainingPlan", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore //verhindert Endlosschleifen bei JSON-Ausgabe
    private List<TrainingSession1> sessions; //Liste aller Trainingseinheiten, die zum Plan gehören
}