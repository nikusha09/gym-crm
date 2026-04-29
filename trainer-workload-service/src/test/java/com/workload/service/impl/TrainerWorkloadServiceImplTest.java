package com.workload.service.impl;

import com.workload.dto.TrainerWorkloadRequest;
import com.workload.model.TrainerWorkload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

    private TrainerWorkloadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TrainerWorkloadServiceImpl();
    }

    private TrainerWorkloadRequest buildRequest(String actionType, int duration) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername("John.Smith");
        request.setTrainerFirstName("John");
        request.setTrainerLastName("Smith");
        request.setActive(true);
        request.setTrainingDate(LocalDate.of(2025, 3, 15));
        request.setTrainingDuration(duration);
        request.setActionType(actionType);
        return request;
    }

    @Test
    void processWorkload_ADD_createsNewTrainerAndAddsWorkload() {
        TrainerWorkloadRequest request = buildRequest("ADD", 60);

        service.processWorkload(request);

        TrainerWorkload workload = service.getWorkload("John.Smith");
        assertNotNull(workload);
        assertEquals("John.Smith", workload.getUsername());
        assertEquals("John", workload.getFirstName());
        assertEquals("Smith", workload.getLastName());
        assertEquals(60, workload.getYearMonthDuration().get(2025).get(3));
    }

    @Test
    void processWorkload_ADD_accumulatesWorkloadForSameMonth() {
        service.processWorkload(buildRequest("ADD", 60));
        service.processWorkload(buildRequest("ADD", 30));

        TrainerWorkload workload = service.getWorkload("John.Smith");
        assertEquals(90, workload.getYearMonthDuration().get(2025).get(3));
    }

    @Test
    void processWorkload_DELETE_subtractsWorkload() {
        service.processWorkload(buildRequest("ADD", 60));
        service.processWorkload(buildRequest("DELETE", 30));

        TrainerWorkload workload = service.getWorkload("John.Smith");
        assertEquals(30, workload.getYearMonthDuration().get(2025).get(3));
    }

    @Test
    void processWorkload_DELETE_doesNotGoBelowZero() {
        service.processWorkload(buildRequest("ADD", 30));
        service.processWorkload(buildRequest("DELETE", 60));

        TrainerWorkload workload = service.getWorkload("John.Smith");
        assertEquals(0, workload.getYearMonthDuration().get(2025).get(3));
    }

    @Test
    void processWorkload_ADD_separateMonths() {
        TrainerWorkloadRequest marchRequest = buildRequest("ADD", 60);
        TrainerWorkloadRequest aprilRequest = buildRequest("ADD", 45);
        aprilRequest.setTrainingDate(LocalDate.of(2025, 4, 10));

        service.processWorkload(marchRequest);
        service.processWorkload(aprilRequest);

        TrainerWorkload workload = service.getWorkload("John.Smith");
        assertEquals(60, workload.getYearMonthDuration().get(2025).get(3));
        assertEquals(45, workload.getYearMonthDuration().get(2025).get(4));
    }

    @Test
    void processWorkload_ADD_separateYears() {
        TrainerWorkloadRequest request2025 = buildRequest("ADD", 60);
        TrainerWorkloadRequest request2026 = buildRequest("ADD", 90);
        request2026.setTrainingDate(LocalDate.of(2026, 3, 15));

        service.processWorkload(request2025);
        service.processWorkload(request2026);

        TrainerWorkload workload = service.getWorkload("John.Smith");
        assertEquals(60, workload.getYearMonthDuration().get(2025).get(3));
        assertEquals(90, workload.getYearMonthDuration().get(2026).get(3));
    }

    @Test
    void getWorkload_throwsException_whenTrainerNotFound() {
        assertThrows(RuntimeException.class, () -> service.getWorkload("unknown.trainer"));
    }
}
