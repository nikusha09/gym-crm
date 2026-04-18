package com.gym.service.impl;

import com.gym.exception.ValidationException;
import com.gym.model.*;
import com.gym.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;

    @BeforeEach
    void setUp() {
        training = new Training();
        training.setTrainingName("Morning Yoga");
        training.setTrainingType(new TrainingType("Yoga"));
        training.setTrainingDate(LocalDate.of(2024, 6, 1));
        training.setTrainingDuration(60);
        training.setTrainee(new Trainee());
        training.setTrainer(new Trainer());
    }

    @Test
    @DisplayName("addTraining: success — saves valid training")
    void addTraining_success() {
        trainingService.addTraining(training);
        verify(trainingRepository).save(training);
    }

    @Test
    @DisplayName("addTraining: blank training name throws ValidationException")
    void addTraining_blankName_throwsValidation() {
        training.setTrainingName("  ");
        assertThrows(ValidationException.class, () -> trainingService.addTraining(training));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    @DisplayName("addTraining: null training type throws ValidationException")
    void addTraining_nullType_throwsValidation() {
        training.setTrainingType(null);
        assertThrows(ValidationException.class, () -> trainingService.addTraining(training));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    @DisplayName("addTraining: null training date throws ValidationException")
    void addTraining_nullDate_throwsValidation() {
        training.setTrainingDate(null);
        assertThrows(ValidationException.class, () -> trainingService.addTraining(training));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    @DisplayName("addTraining: zero duration throws ValidationException")
    void addTraining_zeroDuration_throwsValidation() {
        training.setTrainingDuration(0);
        assertThrows(ValidationException.class, () -> trainingService.addTraining(training));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    @DisplayName("addTraining: negative duration throws ValidationException")
    void addTraining_negativeDuration_throwsValidation() {
        training.setTrainingDuration(-5);
        assertThrows(ValidationException.class, () -> trainingService.addTraining(training));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    @DisplayName("addTraining: null trainee throws ValidationException")
    void addTraining_nullTrainee_throwsValidation() {
        training.setTrainee(null);
        assertThrows(ValidationException.class, () -> trainingService.addTraining(training));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    @DisplayName("addTraining: null trainer throws ValidationException")
    void addTraining_nullTrainer_throwsValidation() {
        training.setTrainer(null);
        assertThrows(ValidationException.class, () -> trainingService.addTraining(training));
        verifyNoInteractions(trainingRepository);
    }

    @Test
    @DisplayName("getTraining: found — returns Optional with training")
    void getTraining_found_returnsOptional() {
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.getTraining(1L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    @DisplayName("getTraining: not found — returns empty Optional")
    void getTraining_notFound_returnsEmpty() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(trainingService.getTraining(99L).isEmpty());
    }

    @Test
    @DisplayName("getAllTrainings: returns list from repository")
    void getAllTrainings_returnsList() {
        when(trainingRepository.findAll()).thenReturn(List.of(training));

        List<Training> result = trainingService.getAllTrainings();

        assertEquals(1, result.size());
        assertEquals(training, result.get(0));
    }
}
