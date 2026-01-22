package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.ExecutionLog;
import com.example.fitnessapp.Model.ExerciseExecutionTemplate;
import com.example.fitnessapp.Model.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
    Optional<ExecutionLog> findBySessionLogAndExerciseTemplate(SessionLog sessionLog, ExerciseExecutionTemplate exerciseTemplate);
}


