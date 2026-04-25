package com.workload.controller;

import com.workload.dto.TrainerWorkloadRequest;
import com.workload.model.TrainerWorkload;
import com.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer-workload")
@RequiredArgsConstructor
public class TrainerWorkloadController {

    private final TrainerWorkloadService trainerWorkloadService;

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkload> getWorkload(@PathVariable String username) {
        TrainerWorkload workload = trainerWorkloadService.getWorkload(username);
        return ResponseEntity.ok(workload);
    }
}
