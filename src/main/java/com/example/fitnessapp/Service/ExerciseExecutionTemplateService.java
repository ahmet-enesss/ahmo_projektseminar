package com.example.fitnessapp.Service;

import com.example.fitnessapp.DTOs.ExerciseExecutionTemplateRequest;
import com.example.fitnessapp.DTOs.ExerciseExecutionTemplateResponse;
import com.example.fitnessapp.Model.Exercise1;
import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Repository.ExerciseExecutionTemplateRepository;
import com.example.fitnessapp.Repository.ExerciseRepository1;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExerciseExecutionTemplateService {

    //Repository für gespeicherte Übngs-Templates
    private final ExerciseExecutionTemplateRepository templateRepository;
    // Repository für Trainingssessions
    private final TrainingSessionRepository1 trainingSessionRepository;
    //Repository für Übungen
    private final ExerciseRepository1 exerciseRepository;

    //Konstruktor-Injection
    public ExerciseExecutionTemplateService(ExerciseExecutionTemplateRepository templateRepository,
                                            TrainingSessionRepository1 trainingSessionRepository,
                                            ExerciseRepository1 exerciseRepository) {
        this.templateRepository = templateRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    //Holt alle Übungen als Liste einer Session und sortiert nach Reihenfolge
    public List<ExerciseExecutionTemplateResponse> getForSession(Long sessionId) {
        //Datenabfrage: liste Von Entities
        return templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(sessionId)
                .stream()
                //jede Entety wird in eine Reponse-DTO umgewandelt
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //Erstellt eine neue Übungsvorlage für eine Session
    public ExerciseExecutionTemplateResponse create(ExerciseExecutionTemplateRequest request) {
        TrainingSession1 session = trainingSessionRepository.findById(request.getSessionId()) //Prüft ob die Trainingssession existiert
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingSession not found"));
        Exercise1 exercise = exerciseRepository.findById(request.getExerciseId())//Prüft ob die Übung existiert
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
        //Prüft ob alle Werte gultig sind
        validate(request);

        //Prüft ob die Reihenfolge (orderIndex) bereits in der Session
        if (templateRepository.existsByTrainingSession_IdAndOrderIndex(session.getId(), request.getOrderIndex())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order index already used in this session");
        }

        //Prüft ob dieselbe Übung bereits in der Session existiert
        if (templateRepository.existsByTrainingSession_IdAndExercise_Id(session.getId(), exercise.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise already added to this session");
        }
        //Baut eine neue Entity aus den Request-Daten
        ExerciseExecutionTemplate entity = ExerciseExecutionTemplate.builder()
                .trainingSession(session)
                .exercise(exercise)
                .plannedSets(request.getPlannedSets())
                .plannedReps(request.getPlannedReps())
                .plannedWeight(request.getPlannedWeight())
                .orderIndex(request.getOrderIndex())
                .build();
        // Speichert die Entity und gibt ein Response-DTO zurück
        return toResponse(templateRepository.save(entity));
    }

    // Aktualisiert eine bestehende Übungsvorlage
    public ExerciseExecutionTemplateResponse update(Long id, ExerciseExecutionTemplateRequest request) {
        ExerciseExecutionTemplate existing = templateRepository.findById(id) // Prüft ob die Vorlage existiert
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));

        // Validiert die Eingabedaten
        validate(request);

        // Prüft nur dann auf Konflikt, wenn sich die Reihenfolge geändert hat
        if (!existing.getOrderIndex().equals(request.getOrderIndex()) &&
                templateRepository.existsByTrainingSession_IdAndOrderIndex(existing.getTrainingSession().getId(),
                        request.getOrderIndex())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order index already used in this session");
        }
        // Neue Übung laden
        Exercise1 exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

        // Verhindert doppelte Übung nach Update
        if (!existing.getExercise().getId().equals(exercise.getId()) &&
                templateRepository.existsByTrainingSession_IdAndExercise_Id(existing.getTrainingSession().getId(), exercise.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exercise already added to this session");
        }
        // Felder aktualisieren
        existing.setExercise(exercise);
        existing.setPlannedSets(request.getPlannedSets());
        existing.setPlannedReps(request.getPlannedReps());
        existing.setPlannedWeight(request.getPlannedWeight());
        existing.setOrderIndex(request.getOrderIndex());

        // Speichern und Response zurückgeben
        return toResponse(templateRepository.save(existing));
    }
    // Löscht eine Übungsvorlage anhand der ID
    public void delete(Long id) {
        templateRepository.deleteById(id);
    }

    // Prüft, ob alle Pflichtwerte gültig sind
    private void validate(ExerciseExecutionTemplateRequest request) {
        if (request.getPlannedSets() == null || request.getPlannedSets() <= 0 ||
                request.getPlannedReps() == null || request.getPlannedReps() <= 0 ||
                request.getPlannedWeight() == null || request.getPlannedWeight() < 0 ||
                request.getOrderIndex() == null || request.getOrderIndex() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid planned values");
        }
    }

    //Wandelt eine Entity in ein Response-DTO um
    private ExerciseExecutionTemplateResponse toResponse(ExerciseExecutionTemplate template) {
        return ExerciseExecutionTemplateResponse.builder()
                .id(template.getId())
                .sessionId(template.getTrainingSession().getId())
                .exerciseId(template.getExercise().getId())
                .exerciseName(template.getExercise().getName())
                .exerciseCategory(template.getExercise().getCategory())
                .plannedSets(template.getPlannedSets())
                .plannedReps(template.getPlannedReps())
                .plannedWeight(template.getPlannedWeight())
                .orderIndex(template.getOrderIndex())
                .build();
    }
}

