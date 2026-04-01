package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.dto.request.AddTrainingRequest;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.GlobalExceptionHandler;
import com.gym.mapper.TrainingMapper;
import com.gym.metrics.TrainingMetrics;
import com.gym.model.Training;
import com.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private TrainingMetrics trainingMetrics;

    @InjectMocks
    private TrainingController trainingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/trainings: 200 on successful training creation")
    void addTraining_success_returns200() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Smith");
        request.setTrainerUsername("Jane.Doe");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 1));
        request.setTrainingDuration(60);

        Training training = new Training();
        when(trainingMapper.toTraining(any())).thenReturn(training);
        doNothing().when(trainingService).addTraining(training);
        doNothing().when(trainingMetrics).incrementTrainingAddedCounter();

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainingMetrics).incrementTrainingAddedCounter();
    }

    @Test
    @DisplayName("POST /api/trainings: 400 when traineeUsername is blank")
    void addTraining_blankTraineeUsername_returns400() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("");
        request.setTrainerUsername("Jane.Doe");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 1));
        request.setTrainingDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/trainings: 400 when trainingDate is null")
    void addTraining_nullDate_returns400() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Smith");
        request.setTrainerUsername("Jane.Doe");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(null);
        request.setTrainingDuration(60);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/trainings: 404 when trainee or trainer not found")
    void addTraining_traineeNotFound_returns404() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("unknown");
        request.setTrainerUsername("Jane.Doe");
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2024, 6, 1));
        request.setTrainingDuration(60);

        when(trainingMapper.toTraining(any()))
                .thenThrow(new EntityNotFoundException("Trainee", "unknown"));

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
