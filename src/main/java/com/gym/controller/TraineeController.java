package com.gym.controller;

import com.gym.dto.request.*;
import com.gym.dto.response.*;
import com.gym.exception.EntityNotFoundException;
import com.gym.mapper.TraineeMapper;
import com.gym.mapper.TrainerMapper;
import com.gym.metrics.TraineeMetrics;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.service.TraineeService;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/trainee")
@Tag(name = "Trainee", description = "Trainee management endpoints")
@Slf4j
public class TraineeController {

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TraineeMapper traineeMapper;
    private TrainerMapper trainerMapper;
    private TraineeMetrics traineeMetrics;

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Autowired
    public void setTraineeMetrics(TraineeMetrics traineeMetrics) {
        this.traineeMetrics = traineeMetrics;
    }

    @PostMapping("/register")
    @Operation(summary = "Register trainee", description = "Create a new trainee profile")
    @ApiResponse(responseCode = "201", description = "Trainee registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {

        log.info("Trainee registration request | firstName={} lastName={}",
                request.getFirstName(), request.getLastName());

        Trainee trainee = traineeMapper.toEntity(request);

        traineeService.createTrainee(trainee);
        traineeMetrics.incrementRegistrationCounter();

        log.info("Trainee registered | username={}", trainee.getUser().getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegistrationResponse(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getPassword()));
    }

    @GetMapping
    @Operation(summary = "Get trainee profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @Valid @RequestBody UsernameRequest request) {

        String username = request.getUsername();
        log.info("Get trainee profile | username={}", username);

        Trainee trainee = traineeService.getTrainee(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", username));

        return ResponseEntity.ok(traineeMapper.toProfileResponse(trainee));
    }

    @PutMapping
    @Operation(summary = "Update trainee profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<UpdateTraineeResponse> updateProfile(
            @RequestHeader("Username") String username,
            @Valid @RequestBody UpdateTraineeRequest request) {
        log.info("Update trainee profile | username={}", username);

        Trainee trainee = traineeService.getTrainee(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", username));

        trainee = traineeMapper.toUpdatedEntity(trainee, request);
        traineeService.updateTrainee(trainee);

        log.info("Trainee profile updated | username={}", request.getUsername());
        return ResponseEntity.ok(traineeMapper.toUpdateResponse(trainee));
    }

    @DeleteMapping
    @Operation(summary = "Delete trainee profile")
    @ApiResponse(responseCode = "200", description = "Trainee deleted")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<Void> deleteProfile(
            @Valid @RequestBody UsernameRequest request) {

        String username = request.getUsername();
        log.info("Delete trainee profile | username={}", username);
        traineeService.deleteByUsername(username);
        log.info("Trainee deleted | username={}", username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unassigned-trainers")
    @Operation(summary = "Get unassigned active trainers for trainee")
    @ApiResponse(responseCode = "200", description = "List retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedTrainers(
            @Valid @RequestBody UsernameRequest request) {

        String username = request.getUsername();
        log.info("Get unassigned trainers | traineeUsername={}", username);

        List<Trainer> trainers = traineeService.getUnassignedTrainers(username);
        List<TrainerSummaryResponse> response = trainers.stream()
                .map(trainer -> trainerMapper.toTrainerSummary(trainer))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/trainers")
    @Operation(summary = "Update trainee's trainer list")
    @ApiResponse(responseCode = "200", description = "Trainer list updated")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainer not found")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTrainers(
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {

        String username = request.getUsername();
        log.info("Update trainers list | traineeUsername={}", username);

        List<Trainer> trainers = request.getTrainerUsernames().stream()
                .map(trainerUsername -> trainerService.getTrainer(trainerUsername)
                        .orElseThrow(() -> new EntityNotFoundException("Trainer", trainerUsername)))
                .toList();

        traineeService.updateTrainers(username, new ArrayList<>(trainers));

        List<TrainerSummaryResponse> response = trainers.stream()
                .map(trainer -> trainerMapper.toTrainerSummary(trainer))
                .toList();

        log.info("Trainer list updated | traineeUsername={}", username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trainings")
    @Operation(summary = "Get trainee trainings list with optional criteria")
    @ApiResponse(responseCode = "200", description = "Trainings retrieved")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(
            @Valid @RequestBody TraineeTrainingsRequest request) {

        String username = request.getUsername();
        log.info("Get trainee trainings | username={}", username);

        List<TraineeTrainingResponse> response = traineeService
                .getTrainings(username, request.getFromDate(), request.getToDate(), request.getTrainerName(), request.getTrainingType())
                .stream()
                .map(training -> traineeMapper.toTraineeTrainingResponse(training))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/activate")
    @Operation(summary = "Activate or deactivate trainee")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<Void> activateDeactivate(
            @Valid @RequestBody ActivateDeactivateRequest request) {

        String username = request.getUsername();
        log.info("Activate/deactivate trainee | username={} isActive={}",
                username, request.getIsActive());

        Trainee trainee = traineeService.getTrainee(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", username));

        if (trainee.getUser().isActive() == request.getIsActive()) {
            throw new IllegalStateException(
                    "Trainee is already " + (request.getIsActive() ? "active" : "inactive"));
        }

        traineeService.activateDeactivate(username, request.getIsActive());
        log.info("Trainee status changed | username={}", username);
        return ResponseEntity.ok().build();
    }
}
