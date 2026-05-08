package com.workload.service.impl;

import com.workload.dto.TrainerWorkloadRequest;
import com.workload.model.TrainerWorkload;
import com.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

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

    private TrainerWorkload buildExistingWorkload(int year, int month, int duration) {
        TrainerWorkload.MonthSummary monthSummary =
                new TrainerWorkload.MonthSummary(month, duration);

        TrainerWorkload.YearSummary yearSummary = new TrainerWorkload.YearSummary();
        yearSummary.setYear(year);
        yearSummary.setMonths(new ArrayList<>());
        yearSummary.getMonths().add(monthSummary);

        TrainerWorkload workload = new TrainerWorkload();
        workload.setUsername("John.Smith");
        workload.setFirstName("John");
        workload.setLastName("Smith");
        workload.setActive(true);
        workload.setYears(new ArrayList<>());
        workload.getYears().add(yearSummary);

        return workload;
    }

    @Test
    void processWorkload_ADD_createsNewTrainerAndAddsWorkload() {
        when(trainerWorkloadRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.empty());

        service.processWorkload(buildRequest("ADD", 60));

        verify(trainerWorkloadRepository).save(argThat(workload ->
                workload.getUsername().equals("John.Smith") &&
                        workload.getFirstName().equals("John") &&
                        workload.getLastName().equals("Smith") &&
                        workload.getYears().get(0).getYear() == 2025 &&
                        workload.getYears().get(0).getMonths().get(0).getMonth() == 3 &&
                        workload.getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration() == 60
        ));
    }

    @Test
    void processWorkload_ADD_accumulatesWorkloadForSameMonth() {
        TrainerWorkload existing = buildExistingWorkload(2025, 3, 60);
        when(trainerWorkloadRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(existing));

        service.processWorkload(buildRequest("ADD", 30));

        verify(trainerWorkloadRepository).save(argThat(workload ->
                workload.getYears().get(0).getMonths().get(0)
                        .getTrainingsSummaryDuration() == 90
        ));
    }

    @Test
    void processWorkload_DELETE_subtractsWorkload() {
        TrainerWorkload existing = buildExistingWorkload(2025, 3, 60);
        when(trainerWorkloadRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(existing));

        service.processWorkload(buildRequest("DELETE", 30));

        verify(trainerWorkloadRepository).save(argThat(workload ->
                workload.getYears().get(0).getMonths().get(0)
                        .getTrainingsSummaryDuration() == 30
        ));
    }

    @Test
    void processWorkload_DELETE_doesNotGoBelowZero() {
        TrainerWorkload existing = buildExistingWorkload(2025, 3, 30);
        when(trainerWorkloadRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(existing));

        service.processWorkload(buildRequest("DELETE", 60));

        verify(trainerWorkloadRepository).save(argThat(workload ->
                workload.getYears().get(0).getMonths().get(0)
                        .getTrainingsSummaryDuration() == 0
        ));
    }

    @Test
    void processWorkload_ADD_newYearCreated() {
        TrainerWorkload existing = buildExistingWorkload(2025, 3, 60);
        when(trainerWorkloadRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(existing));

        TrainerWorkloadRequest request2026 = buildRequest("ADD", 90);
        request2026.setTrainingDate(LocalDate.of(2026, 3, 15));

        service.processWorkload(request2026);

        verify(trainerWorkloadRepository).save(argThat(workload ->
                workload.getYears().size() == 2 &&
                        workload.getYears().get(1).getYear() == 2026 &&
                        workload.getYears().get(1).getMonths().get(0)
                                .getTrainingsSummaryDuration() == 90
        ));
    }

    @Test
    void processWorkload_ADD_newMonthCreated() {
        TrainerWorkload existing = buildExistingWorkload(2025, 3, 60);
        when(trainerWorkloadRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(existing));

        TrainerWorkloadRequest aprilRequest = buildRequest("ADD", 45);
        aprilRequest.setTrainingDate(LocalDate.of(2025, 4, 10));

        service.processWorkload(aprilRequest);

        verify(trainerWorkloadRepository).save(argThat(workload ->
                workload.getYears().get(0).getMonths().size() == 2 &&
                        workload.getYears().get(0).getMonths().get(1).getMonth() == 4 &&
                        workload.getYears().get(0).getMonths().get(1)
                                .getTrainingsSummaryDuration() == 45
        ));
    }

    @Test
    void getWorkload_returnsWorkload_whenTrainerExists() {
        TrainerWorkload existing = buildExistingWorkload(2025, 3, 60);
        when(trainerWorkloadRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(existing));

        TrainerWorkload result = service.getWorkload("John.Smith");

        assertNotNull(result);
        assertEquals("John.Smith", result.getUsername());
        verify(trainerWorkloadRepository).findByUsername("John.Smith");
    }

    @Test
    void getWorkload_throwsException_whenTrainerNotFound() {
        when(trainerWorkloadRepository.findByUsername("unknown.trainer"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getWorkload("unknown.trainer"));
    }
}
