package com.gym.controller;

import com.gym.dto.request.*;
import com.gym.dto.response.*;
import com.gym.exception.EntityNotFoundException;
import com.gym.mapper.TrainerMapper;
import com.gym.metrics.TrainerMetrics;
import com.gym.model.Trainer;
import com.gym.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
@Tag(name = "Trainer", description = "Trainer management endpoints")
@Slf4j
public class TrainerController {

    private TrainerService trainerService;
    private TrainerMapper trainerMapper;
    private TrainerMetrics trainerMetrics;

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Autowired
    public void setTrainerMetrics(TrainerMetrics trainerMetrics) {
        this.trainerMetrics = trainerMetrics;
    }

    @PostMapping("/register")
    @Operation(summary = "Register trainer", description = "Create a new trainer profile")
    @ApiResponse(responseCode = "201", description = "Trainer registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        log.info("Trainer registration request | firstName={} lastName={}",
                request.getFirstName(), request.getLastName());

        Trainer trainer = trainerMapper.toEntity(request);

        trainerService.createTrainer(trainer);
        trainerMetrics.incrementRegistrationCounter();

        log.info("Trainer registered | username={}", trainer.getUser().getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegistrationResponse(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getPassword()));
    }

    @GetMapping
    @Operation(summary = "Get trainer profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @Valid @RequestBody UsernameRequest request) {

        String username = request.getUsername();
        log.info("Get trainer profile | username={}", username);

        Trainer trainer = trainerService.getTrainer(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", username));

        return ResponseEntity.ok(trainerMapper.toProfileResponse(trainer));
    }

    @PutMapping
    @Operation(summary = "Update trainer profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<UpdateTrainerResponse> updateProfile(
            @RequestHeader("Username") String username,
            @Valid @RequestBody UpdateTrainerRequest request) {
        log.info("Update trainer profile | username={}", username);

        Trainer trainer = trainerService.getTrainer(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", username));

        trainer = trainerMapper.toUpdatedEntity(trainer, request);
        trainerService.updateTrainer(trainer);

        log.info("Trainer profile updated | username={}", username);
        return ResponseEntity.ok(trainerMapper.toUpdateResponse(trainer));
    }

    @GetMapping("/trainings")
    @Operation(summary = "Get trainer trainings list with optional criteria")
    @ApiResponse(responseCode = "200", description = "Trainings retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(
            @Valid @RequestBody TrainerTrainingsRequest request) {
        String username = request.getUsername();
        log.info("Get trainer trainings | username={}", username);

        List<TrainerTrainingResponse> response = trainerService
                .getTrainings(username, request.getFromDate(), request.getToDate(),
                        request.getTraineeName())
                .stream()
                .map(t -> trainerMapper.toTrainerTrainingResponse(t))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/activate")
    @Operation(summary = "Activate or deactivate trainer")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<Void> activateDeactivate(
            @Valid @RequestBody ActivateDeactivateRequest request) {
        String username = request.getUsername();
        log.info("Activate/deactivate trainer | username={} isActive={}",
                username, request.getIsActive());

        Trainer trainer = trainerService.getTrainer(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", username));

        if (trainer.getUser().isActive() == request.getIsActive()) {
            throw new IllegalStateException(
                    "Trainer is already " + (request.getIsActive() ? "active" : "inactive"));
        }

        trainerService.activateDeactivate(username, request.getIsActive());
        log.info("Trainer status changed | username={}", username);
        return ResponseEntity.ok().build();
    }
}
