package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.SessionLog;
import com.example.fitnessapp.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {
    
    @Query("SELECT COUNT(sl) FROM SessionLog sl WHERE sl.templateSession.id = :sessionId")
    long countByTemplateSession_Id(@Param("sessionId") Long sessionId);
    
    // Findet alle SessionLogs für einen bestimmten Benutzer (Trainingshistorie)
    List<SessionLog> findByUserOrderByStartTimeDesc(User user);
    
    // Findet ein SessionLog anhand der ID, nur wenn es dem Benutzer gehört
    Optional<SessionLog> findByIdAndUser(Long id, User user);
}


