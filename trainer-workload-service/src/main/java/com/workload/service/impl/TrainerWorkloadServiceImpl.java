package com.workload.service.impl;

import com.workload.dto.TrainerWorkloadRequest;
import com.workload.model.TrainerWorkload;
import com.workload.service.TrainerWorkloadService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final Map<String, TrainerWorkload> workloadStore = new ConcurrentHashMap<>();

    @Override
    public void processWorkload(TrainerWorkloadRequest request) {
        String username = request.getTrainerUsername();
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        int duration = request.getTrainingDuration();

        TrainerWorkload workload = workloadStore.computeIfAbsent(username, k -> {
            TrainerWorkload newWorkload = new TrainerWorkload();
            newWorkload.setUsername(username);
            newWorkload.setFirstName(request.getTrainerFirstName());
            newWorkload.setLastName(request.getTrainerLastName());
            newWorkload.setActive(request.isActive());
            newWorkload.setYearMonthDuration(new HashMap<>());
            return newWorkload;
        });

        Map<Integer, Map<Integer, Integer>> yearMap = workload.getYearMonthDuration();
        yearMap.computeIfAbsent(year, k -> new HashMap<>());

        if ("ADD".equalsIgnoreCase(request.getActionType())) {
            yearMap.get(year).merge(month, duration, Integer::sum);
        } else if ("DELETE".equalsIgnoreCase(request.getActionType())) {
            yearMap.get(year).merge(month, duration, (existing, val) -> Math.max(0, existing - val));
        }
    }

    @Override
    public TrainerWorkload getWorkload(String username) {
        TrainerWorkload workload = workloadStore.get(username);
        if (workload == null) {
            throw new RuntimeException("No workload found for trainer: " + username);
        }
        return workload;
    }
}
