package com.workload.controller;

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

import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @InjectMocks
    private TrainerWorkloadController trainerWorkloadController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerWorkloadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getWorkload_returnsWorkload() throws Exception {
        TrainerWorkload.MonthSummary monthSummary = new TrainerWorkload.MonthSummary(3, 60);

        TrainerWorkload.YearSummary yearSummary = new TrainerWorkload.YearSummary();
        yearSummary.setYear(2025);
        yearSummary.setMonths(new ArrayList<>());
        yearSummary.getMonths().add(monthSummary);

        TrainerWorkload workload = new TrainerWorkload();
        workload.setUsername("John.Smith");
        workload.setFirstName("John");
        workload.setLastName("Smith");
        workload.setActive(true);
        workload.setYears(new ArrayList<>());
        workload.getYears().add(yearSummary);

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
