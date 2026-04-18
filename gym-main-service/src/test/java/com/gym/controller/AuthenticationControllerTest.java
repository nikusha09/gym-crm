package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.request.ChangePasswordRequest;
import com.gym.exception.GlobalExceptionHandler;
import com.gym.model.Trainee;
import com.gym.model.User;
import com.gym.security.jwt.JwtUtil;
import com.gym.security.service.LoginAttemptService;
import com.gym.security.service.TokenBlacklistService;
import com.gym.service.TraineeService;
import com.gym.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        User user = new User();
        user.setUsername("John.Smith");
        user.setPassword("encodedPassword");
        trainee = new Trainee();
        trainee.setUser(user);
    }

    @Test
    @DisplayName("POST /api/auth/login: 200 with JWT token on valid credentials")
    void login_validCredentials_returns200() throws Exception {
        when(loginAttemptService.isBlocked("John.Smith")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("John.Smith", "pass123456"));
        when(jwtUtil.generateToken("John.Smith")).thenReturn("mocked.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .header("Username", "John.Smith")
                        .header("Password", "pass123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));

        verify(loginAttemptService).loginSucceeded("John.Smith");
    }

    @Test
    @DisplayName("POST /api/auth/login: 401 on invalid credentials")
    void login_invalidCredentials_returns401() throws Exception {
        when(loginAttemptService.isBlocked("John.Smith")).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .header("Username", "John.Smith")
                        .header("Password", "wrongPassword"))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService).loginFailed("John.Smith");
    }

    @Test
    @DisplayName("POST /api/auth/login: 429 when user is blocked")
    void login_blockedUser_returns429() throws Exception {
        when(loginAttemptService.isBlocked("John.Smith")).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .header("Username", "John.Smith")
                        .header("Password", "pass123456"))
                .andExpect(status().isTooManyRequests());

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("POST /api/auth/logout: 200 and token blacklisted")
    void logout_validToken_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk());

        verify(tokenBlacklistService).blacklist("mocked.jwt.token");
    }


    @Test
    @DisplayName("PUT /api/auth/password: 200 when trainee changes password")
    void changePassword_trainee_returns200() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("John.Smith");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        when(traineeService.getTrainee("John.Smith")).thenReturn(Optional.of(trainee));

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).changePassword("John.Smith", "oldPass", "newPass");
        verifyNoInteractions(trainerService);
    }

    @Test
    @DisplayName("PUT /api/auth/password: 200 when trainer changes password")
    void changePassword_trainer_returns200() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("Jane.Doe");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        when(traineeService.getTrainee("Jane.Doe")).thenReturn(Optional.empty());
        when(trainerService.getTrainer("Jane.Doe"))
                .thenReturn(Optional.of(new com.gym.model.Trainer()));

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).changePassword("Jane.Doe", "oldPass", "newPass");
    }

    @Test
    @DisplayName("PUT /api/auth/password: 404 when user not found")
    void changePassword_userNotFound_returns404() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("unknown");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        when(traineeService.getTrainee("unknown")).thenReturn(Optional.empty());
        when(trainerService.getTrainer("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/auth/password: 400 when request fields are blank")
    void changePassword_blankFields_returns400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("");
        request.setOldPassword("");
        request.setNewPassword("");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
