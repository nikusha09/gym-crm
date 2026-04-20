package com.workload.service;

import com.workload.dto.TrainerWorkloadRequest;
import com.workload.model.TrainerWorkload;

public interface TrainerWorkloadService {
    void processWorkload(TrainerWorkloadRequest request);
    TrainerWorkload getWorkload(String username);
}
