package com.example.fitnessapp.service;

import com.example.fitnessapp.Model.TrainingSession1;
import com.example.fitnessapp.Model.TrainingSessionStatus;
import com.example.fitnessapp.Repository.TrainingSessionRepository1;
import com.example.fitnessapp.Service.TrainingSessionService1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceUnitTest {

    @Mock
    private TrainingSessionRepository1 sessionRepository;

    @InjectMocks
    private TrainingSessionService1 service;

    private TrainingSession1 session;

    @BeforeEach
    void setUp() {
        session = TrainingSession1.builder().id(5L).name("S").build();
    }

    @Test
    void getAll_returnsList() {
        when(sessionRepository.findAll()).thenReturn(List.of(session));

        var all = service.getAllTrainingSessions();
        assertEquals(1, all.size());
    }

    @Test
    void getById_notFound_throws() {
        when(sessionRepository.findById(9L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getTrainingSessionById(9L));
        assertTrue(ex.getMessage().contains("TrainingSession not found"));
    }

    @Test
    void deprecatedCreate_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createTrainingSession(1L, "n", null, TrainingSessionStatus.GEPLANT, Set.of()));
        assertTrue(ex.getMessage().contains("veraltet"));
    }

    @Test
    void delete_callsRepository() {
        service.deleteTrainingSession(4L);
        verify(sessionRepository).deleteById(4L);
    }
}
