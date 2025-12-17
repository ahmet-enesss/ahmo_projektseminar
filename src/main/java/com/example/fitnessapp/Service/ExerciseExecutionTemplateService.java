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

    private final ExerciseExecutionTemplateRepository templateRepository;
    private final TrainingSessionRepository1 trainingSessionRepository;
    private final ExerciseRepository1 exerciseRepository;

    public ExerciseExecutionTemplateService(ExerciseExecutionTemplateRepository templateRepository,
                                            TrainingSessionRepository1 trainingSessionRepository,
                                            ExerciseRepository1 exerciseRepository) {
        this.templateRepository = templateRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public List<ExerciseExecutionTemplateResponse> getForSession(Long sessionId) {
        return templateRepository.findByTrainingSession_IdOrderByOrderIndexAsc(sessionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExerciseExecutionTemplateResponse create(ExerciseExecutionTemplateRequest request) {
        TrainingSession1 session = trainingSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TrainingSession not found"));
        Exercise1 exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

        validate(request);

        if (templateRepository.existsByTrainingSession_IdAndOrderIndex(session.getId(), request.getOrderIndex())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order index already used in this session");
        }

        ExerciseExecutionTemplate entity = ExerciseExecutionTemplate.builder()
                .trainingSession(session)
                .exercise(exercise)
                .plannedSets(request.getPlannedSets())
                .plannedReps(request.getPlannedReps())
                .plannedWeight(request.getPlannedWeight())
                .orderIndex(request.getOrderIndex())
                .build();

        return toResponse(templateRepository.save(entity));
    }

    public ExerciseExecutionTemplateResponse update(Long id, ExerciseExecutionTemplateRequest request) {
        ExerciseExecutionTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));

        validate(request);

        if (!existing.getOrderIndex().equals(request.getOrderIndex()) &&
                templateRepository.existsByTrainingSession_IdAndOrderIndex(existing.getTrainingSession().getId(),
                        request.getOrderIndex())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order index already used in this session");
        }

        Exercise1 exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

        existing.setExercise(exercise);
        existing.setPlannedSets(request.getPlannedSets());
        existing.setPlannedReps(request.getPlannedReps());
        existing.setPlannedWeight(request.getPlannedWeight());
        existing.setOrderIndex(request.getOrderIndex());

        return toResponse(templateRepository.save(existing));
    }

    public void delete(Long id) {
        templateRepository.deleteById(id);
    }

    private void validate(ExerciseExecutionTemplateRequest request) {
        if (request.getPlannedSets() == null || request.getPlannedSets() <= 0 ||
                request.getPlannedReps() == null || request.getPlannedReps() <= 0 ||
                request.getPlannedWeight() == null || request.getPlannedWeight() < 0 ||
                request.getOrderIndex() == null || request.getOrderIndex() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid planned values");
        }
    }

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


