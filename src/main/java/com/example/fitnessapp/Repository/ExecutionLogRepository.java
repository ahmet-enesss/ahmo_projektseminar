package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
}


