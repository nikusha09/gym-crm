package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.dto.request.*;
import com.gym.dto.response.*;
import com.gym.exception.GlobalExceptionHandler;
import com.gym.mapper.TrainerMapper;
import com.gym.metrics.TrainerMetrics;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainerMetrics trainerMetrics;

    @InjectMocks
    private TrainerController trainerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = new User();
        user.setUsername("Jane.Doe");
        user.setPassword("pass123456");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setActive(true);

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
    }

    @Test
    @DisplayName("POST /api/trainer/register: 201 with username and password")
    void register_success_returns201() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setSpecializationId(1L);

        when(trainerMapper.toEntity(any())).thenReturn(trainer);
        doNothing().when(trainerService).createTrainer(trainer);
        doNothing().when(trainerMetrics).incrementRegistrationCounter();

        mockMvc.perform(post("/api/trainer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Jane.Doe"))
                .andExpect(jsonPath("$.password").value("pass123456"));

        verify(trainerMetrics).incrementRegistrationCounter();
    }

    @Test
    @DisplayName("POST /api/trainer/register: 400 when firstName is blank")
    void register_blankFirstName_returns400() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("");
        request.setLastName("Doe");
        request.setSpecializationId(1L);

        mockMvc.perform(post("/api/trainer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/trainer/register: 400 when specializationId is null")
    void register_nullSpecializationId_returns400() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setSpecializationId(null);

        mockMvc.perform(post("/api/trainer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/trainer: 200 with profile response")
    void getProfile_found_returns200() throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername("Jane.Doe");

        TrainerProfileResponse profile = new TrainerProfileResponse(
                "Jane", "Doe", 1L, true, List.of());

        when(trainerService.getTrainer("Jane.Doe")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(profile);

        mockMvc.perform(get("/api/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("GET /api/trainer: 404 when trainer not found")
    void getProfile_notFound_returns404() throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername("unknown");

        when(trainerService.getTrainer("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/trainer: 200 with updated profile")
    void updateProfile_success_returns200() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setUsername("Jane.Doe");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setIsActive(true);

        UpdateTrainerResponse updateResponse = new UpdateTrainerResponse(
                "Jane.Doe", "Jane", "Doe", 1L, true, List.of());

        when(trainerService.getTrainer("Jane.Doe")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toUpdatedEntity(any(Trainer.class), any(UpdateTrainerRequest.class)))
                .thenReturn(trainer);
        when(trainerMapper.toUpdateResponse(trainer)).thenReturn(updateResponse);

        mockMvc.perform(put("/api/trainer")
                        .header("Username", "Jane.Doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane.Doe"));
    }

    @Test
    @DisplayName("PUT /api/trainer: 404 when trainer not found")
    void updateProfile_notFound_returns404() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setUsername("unknown");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setIsActive(true);

        when(trainerService.getTrainer("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/trainer")
                        .header("Username", "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/trainer/trainings: 200 with empty list")
    void getTrainings_returns200() throws Exception {
        TrainerTrainingsRequest request = new TrainerTrainingsRequest();
        request.setUsername("Jane.Doe");

        when(trainerService.getTrainings("Jane.Doe", null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainer/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PATCH /api/trainer/activate: 200 on status change")
    void activateDeactivate_success_returns200() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("Jane.Doe");
        request.setIsActive(false);

        when(trainerService.getTrainer("Jane.Doe")).thenReturn(Optional.of(trainer));

        mockMvc.perform(patch("/api/trainer/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).activateDeactivate("Jane.Doe", false);
    }

    @Test
    @DisplayName("PATCH /api/trainer/activate: 409 when already in requested state")
    void activateDeactivate_alreadyInState_returns409() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("Jane.Doe");
        request.setIsActive(true);

        when(trainerService.getTrainer("Jane.Doe")).thenReturn(Optional.of(trainer));

        mockMvc.perform(patch("/api/trainer/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /api/trainer/activate: 404 when trainer not found")
    void activateDeactivate_notFound_returns404() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("unknown");
        request.setIsActive(true);

        when(trainerService.getTrainer("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/trainer/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/trainer/activate: 400 when isActive is null")
    void activateDeactivate_nullIsActive_returns400() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("Jane.Doe");
        request.setIsActive(null);

        mockMvc.perform(patch("/api/trainer/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
