package com.gym.service.impl;

import com.gym.model.TrainingType;
import com.gym.repository.TrainingTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    @DisplayName("findByName: found — returns Optional with training type")
    void findByName_found_returnsOptional() {
        TrainingType yoga = new TrainingType("Yoga");
        when(trainingTypeRepository.findByTrainingTypeName("Yoga")).thenReturn(Optional.of(yoga));

        Optional<TrainingType> result = trainingTypeService.findByName("Yoga");

        assertTrue(result.isPresent());
        assertEquals("Yoga", result.get().getTrainingTypeName());
    }

    @Test
    @DisplayName("findByName: not found — returns empty Optional")
    void findByName_notFound_returnsEmpty() {
        when(trainingTypeRepository.findByTrainingTypeName("Unknown")).thenReturn(Optional.empty());

        assertTrue(trainingTypeService.findByName("Unknown").isEmpty());
    }

    @Test
    @DisplayName("findAll: returns all training types from repository")
    void findAll_returnsList() {
        List<TrainingType> types = List.of(new TrainingType("Yoga"), new TrainingType("Cardio"));
        when(trainingTypeRepository.findAll()).thenReturn(types);

        List<TrainingType> result = trainingTypeService.findAll();

        assertEquals(2, result.size());
        assertEquals("Yoga", result.get(0).getTrainingTypeName());
        assertEquals("Cardio", result.get(1).getTrainingTypeName());
    }

    @Test
    @DisplayName("findAll: empty repository — returns empty list")
    void findAll_emptyRepository_returnsEmptyList() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        List<TrainingType> result = trainingTypeService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
