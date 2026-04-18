package com.gym.controller;

import com.gym.exception.GlobalExceptionHandler;
import com.gym.model.TrainingType;
import com.gym.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingTypeController trainingTypeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/training-types: 200 with list of training types")
    void getAllTrainingTypes_returns200() throws Exception {
        when(trainingTypeService.findAll()).thenReturn(List.of(
                new TrainingType(1L, "Yoga"),
                new TrainingType(2L, "Cardio")
        ));

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].trainingTypeName").value("Yoga"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].trainingTypeName").value("Cardio"));

        verify(trainingTypeService).findAll();
    }

    @Test
    @DisplayName("GET /api/training-types: 200 with empty list")
    void getAllTrainingTypes_emptyList_returns200() throws Exception {
        when(trainingTypeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
