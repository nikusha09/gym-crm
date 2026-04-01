package com.gym.controller;

import com.gym.dto.request.ChangePasswordRequest;
import com.gym.exception.EntityNotFoundException;
import com.gym.service.AuthenticationService;
import com.gym.service.TraineeService;
import com.gym.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and password change")
@Slf4j
public class AuthenticationController {

    private AuthenticationService authenticationService;
    private TraineeService traineeService;
    private TrainerService trainerService;

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @GetMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username and password")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<Void> login(
            @RequestHeader("Username") String username,
            @RequestHeader("Password") String password) {

        log.info("Login attempt | username={}", username);
        authenticationService.authenticate(username, password);
        log.info("Login successful | username={}", username);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change login password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Authentication failed")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Change password request | username={}", request.getUsername());

        if (traineeService.getTrainee(request.getUsername()).isPresent()) {
            traineeService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        } else if (trainerService.getTrainer(request.getUsername()).isPresent()) {
            trainerService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        } else {
            throw new EntityNotFoundException("User", request.getUsername());
        }

        log.info("Password changed successfully | username={}", request.getUsername());
        return ResponseEntity.ok().build();
    }
}
