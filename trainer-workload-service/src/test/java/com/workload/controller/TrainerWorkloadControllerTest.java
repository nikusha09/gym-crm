package com.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.workload.dto.TrainerWorkloadRequest;
import com.workload.exception.GlobalExceptionHandler;
import com.workload.model.TrainerWorkload;
import com.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @InjectMocks
    private TrainerWorkloadController trainerWorkloadController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerWorkloadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private TrainerWorkloadRequest buildRequest() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername("John.Smith");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Smith");
        request.setActive(true);
        request.setTrainingDate(LocalDate.of(2025, 3, 15));
        request.setTrainingDuration(60);
        request.setActionType("ADD");
        return request;
    }

    @Test
    void processWorkload_returnsOk() throws Exception {
        doNothing().when(trainerWorkloadService).processWorkload(any());

        mockMvc.perform(post("/api/trainer-workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        verify(trainerWorkloadService, times(1)).processWorkload(any());
    }

    @Test
    void getWorkload_returnsWorkload() throws Exception {
        Map<Integer, Map<Integer, Integer>> yearMonthDuration = new HashMap<>();
        Map<Integer, Integer> monthDuration = new HashMap<>();
        monthDuration.put(3, 60);
        yearMonthDuration.put(2025, monthDuration);

        TrainerWorkload workload = new TrainerWorkload(
                "John.Smith", "John", "Smith", true, yearMonthDuration);

        when(trainerWorkloadService.getWorkload("John.Smith")).thenReturn(workload);

        mockMvc.perform(get("/api/trainer-workload/John.Smith")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(trainerWorkloadService, times(1)).getWorkload("John.Smith");
    }

    @Test
    void getWorkload_returnsNotFound_whenTrainerDoesNotExist() throws Exception {
        when(trainerWorkloadService.getWorkload("unknown.trainer"))
                .thenThrow(new RuntimeException("No workload found for trainer: unknown.trainer"));

        mockMvc.perform(get("/api/trainer-workload/unknown.trainer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(trainerWorkloadService, times(1)).getWorkload("unknown.trainer");
    }
}
