package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {
}


