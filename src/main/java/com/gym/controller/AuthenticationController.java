package com.gym.controller;

import com.gym.dto.request.ChangePasswordRequest;
import com.gym.exception.EntityNotFoundException;
import com.gym.security.jwt.JwtUtil;
import com.gym.security.service.LoginAttemptService;
import com.gym.security.service.TokenBlacklistService;
import com.gym.service.TraineeService;
import com.gym.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, logout and password change")
@Slf4j
public class AuthenticationController {

    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private LoginAttemptService loginAttemptService;
    private TokenBlacklistService tokenBlacklistService;
    private TraineeService traineeService;
    private TrainerService trainerService;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setLoginAttemptService(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Autowired
    public void setTokenBlacklistService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and receive JWT token")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "429", description = "Too many failed attempts")
    public ResponseEntity<Map<String, String>> login(
            @RequestHeader("Username") String username,
            @RequestHeader("Password") String password) {

        log.info("Login attempt | username={}", username);

        if (loginAttemptService.isBlocked(username)) {
            log.warn("Blocked login attempt | username={}", username);
            return ResponseEntity.status(429)
                    .body(Map.of("error",
                            "Account blocked due to too many failed attempts. Try again in 5 minutes."));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            loginAttemptService.loginSucceeded(username);
            String token = jwtUtil.generateToken(username);

            log.info("Login successful | username={}", username);
            return ResponseEntity.ok(Map.of("token", token));

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(username);
            log.warn("Login failed | username={}", username);
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate JWT token")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklist(token);
            log.info("User logged out successfully");
        }

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
            traineeService.changePassword(request.getUsername(),
                    request.getOldPassword(), request.getNewPassword());
        } else if (trainerService.getTrainer(request.getUsername()).isPresent()) {
            trainerService.changePassword(request.getUsername(),
                    request.getOldPassword(), request.getNewPassword());
        } else {
            throw new EntityNotFoundException("User", request.getUsername());
        }

        log.info("Password changed successfully | username={}", request.getUsername());
        return ResponseEntity.ok().build();
    }
}
