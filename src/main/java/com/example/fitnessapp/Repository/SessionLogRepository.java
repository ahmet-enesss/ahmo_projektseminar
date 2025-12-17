package com.example.fitnessapp.Repository;

import com.example.fitnessapp.Model.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {
    
    @Query("SELECT COUNT(sl) FROM SessionLog sl WHERE sl.templateSession.id = :sessionId")
    long countByTemplateSession_Id(@Param("sessionId") Long sessionId);
}


