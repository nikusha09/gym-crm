package com.workload.service.impl;

import com.workload.dto.TrainerWorkloadRequest;
import com.workload.model.TrainerWorkload;
import com.workload.repository.TrainerWorkloadRepository;
import com.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;

    @Override
    public void processWorkload(TrainerWorkloadRequest request) {
        String transactionId = MDC.get("transactionId");
        log.info("Processing workload | transactionId={} trainer={} action={}",
                transactionId, request.getTrainerUsername(), request.getActionType());

        Optional<TrainerWorkload> existing = trainerWorkloadRepository
                .findByUsername(request.getTrainerUsername());

        TrainerWorkload workload;

        if (existing.isEmpty()) {
            log.info("Trainer not found, creating new record | trainer={}",
                    request.getTrainerUsername());
            workload = createNewWorkload(request);
        } else {
            log.info("Trainer found, updating record | trainer={}",
                    request.getTrainerUsername());
            workload = updateExistingWorkload(existing.get(), request);
        }

        trainerWorkloadRepository.save(workload);
        log.info("Workload saved successfully | transactionId={} trainer={}",
                transactionId, request.getTrainerUsername());
    }

    @Override
    public TrainerWorkload getWorkload(String username) {
        String transactionId = MDC.get("transactionId");
        log.info("Fetching workload | transactionId={} trainer={}", transactionId, username);

        return trainerWorkloadRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "No workload found for trainer: " + username));
    }

    private TrainerWorkload createNewWorkload(TrainerWorkloadRequest request) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        int duration = request.getTrainingDuration();

        TrainerWorkload.MonthSummary monthSummary =
                new TrainerWorkload.MonthSummary(month, duration);

        TrainerWorkload.YearSummary yearSummary = new TrainerWorkload.YearSummary();
        yearSummary.setYear(year);
        yearSummary.setMonths(new ArrayList<>());
        yearSummary.getMonths().add(monthSummary);

        TrainerWorkload workload = new TrainerWorkload();
        workload.setUsername(request.getTrainerUsername());
        workload.setFirstName(request.getTrainerFirstName());
        workload.setLastName(request.getTrainerLastName());
        workload.setActive(request.isActive());
        workload.setYears(new ArrayList<>());
        workload.getYears().add(yearSummary);

        return workload;
    }

    private TrainerWorkload updateExistingWorkload(TrainerWorkload workload,
                                                   TrainerWorkloadRequest request) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        int duration = request.getTrainingDuration();

        TrainerWorkload.YearSummary yearSummary = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    TrainerWorkload.YearSummary newYear = new TrainerWorkload.YearSummary();
                    newYear.setYear(year);
                    newYear.setMonths(new ArrayList<>());
                    workload.getYears().add(newYear);
                    return newYear;
                });

        TrainerWorkload.MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    TrainerWorkload.MonthSummary newMonth =
                            new TrainerWorkload.MonthSummary(month, 0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        if ("ADD".equalsIgnoreCase(request.getActionType())) {
            monthSummary.setTrainingsSummaryDuration(
                    monthSummary.getTrainingsSummaryDuration() + duration);
        } else if ("DELETE".equalsIgnoreCase(request.getActionType())) {
            monthSummary.setTrainingsSummaryDuration(
                    Math.max(0, monthSummary.getTrainingsSummaryDuration() - duration));
        }

        log.info("Updated duration | year={} month={} newDuration={} action={}",
                year, month, monthSummary.getTrainingsSummaryDuration(),
                request.getActionType());

        return workload;
    }
}
