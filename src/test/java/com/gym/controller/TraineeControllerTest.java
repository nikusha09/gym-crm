package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.GlobalExceptionHandler;
import com.gym.dto.request.*;
import com.gym.dto.response.*;
import com.gym.mapper.TraineeMapper;
import com.gym.mapper.TrainerMapper;
import com.gym.metrics.TraineeMetrics;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TraineeMetrics traineeMetrics;

    @InjectMocks
    private TraineeController traineeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = new User();
        user.setUsername("John.Smith");
        user.setPassword("pass123456");
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setActive(true);

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(user);
    }

    @Test
    @DisplayName("POST /api/trainee/register: 201 with username and password")
    void register_success_returns201() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Smith");

        when(traineeMapper.toEntity(any())).thenReturn(trainee);
        doNothing().when(traineeService).createTrainee(trainee);
        doNothing().when(traineeMetrics).incrementRegistrationCounter();

        mockMvc.perform(post("/api/trainee/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.password").value("pass123456"));

        verify(traineeMetrics).incrementRegistrationCounter();
    }

    @Test
    @DisplayName("POST /api/trainee/register: 400 when firstName is blank")
    void register_blankFirstName_returns400() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("");
        request.setLastName("Smith");

        mockMvc.perform(post("/api/trainee/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/trainee: 200 with profile response")
    void getProfile_found_returns200() throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername("John.Smith");

        TraineeProfileResponse profile = new TraineeProfileResponse(
                "John", "Smith", null, null, true, List.of());

        when(traineeService.getTrainee("John.Smith")).thenReturn(Optional.of(trainee));
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(profile);

        mockMvc.perform(get("/api/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @DisplayName("GET /api/trainee: 404 when trainee not found")
    void getProfile_notFound_returns404() throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername("unknown");

        when(traineeService.getTrainee("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/trainee: 200 with updated profile")
    void updateProfile_success_returns200() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setUsername("John.Smith");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setIsActive(true);

        UpdateTraineeResponse updateResponse = new UpdateTraineeResponse(
                "John.Smith", "John", "Smith", null, null, true, List.of());

        when(traineeService.getTrainee("John.Smith")).thenReturn(Optional.of(trainee));
        when(traineeMapper.toUpdatedEntity(any(Trainee.class), any(UpdateTraineeRequest.class)))
                .thenReturn(trainee);
        when(traineeMapper.toUpdateResponse(trainee)).thenReturn(updateResponse);

        mockMvc.perform(put("/api/trainee")
                        .header("Username", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"));
    }

    @Test
    @DisplayName("PUT /api/trainee: 404 when trainee not found")
    void updateProfile_notFound_returns404() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setUsername("unknown");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setIsActive(true);

        when(traineeService.getTrainee("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/trainee")
                        .header("Username", "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/trainee: 200 on successful delete")
    void deleteProfile_success_returns200() throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername("John.Smith");

        doNothing().when(traineeService).deleteByUsername("John.Smith");

        mockMvc.perform(delete("/api/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).deleteByUsername("John.Smith");
    }

    @Test
    @DisplayName("DELETE /api/trainee: 404 when trainee not found")
    void deleteProfile_notFound_returns404() throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername("unknown");

        doThrow(new EntityNotFoundException("Trainee", "unknown"))
                .when(traineeService).deleteByUsername("unknown");

        mockMvc.perform(delete("/api/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/trainee/unassigned-trainers: 200 with trainer list")
    void getUnassignedTrainers_returns200() throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername("John.Smith");

        Trainer trainer = new Trainer();
        User trainerUser = new User();
        trainerUser.setUsername("Jane.Doe");
        trainer.setUser(trainerUser);

        TrainerSummaryResponse summary = new TrainerSummaryResponse("Jane.Doe", "Jane", "Doe", 1L);

        when(traineeService.getUnassignedTrainers("John.Smith")).thenReturn(List.of(trainer));
        when(trainerMapper.toTrainerSummary(trainer)).thenReturn(summary);

        mockMvc.perform(get("/api/trainee/unassigned-trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("Jane.Doe"));
    }

    @Test
    @DisplayName("PUT /api/trainee/trainers: 200 with updated trainer list")
    void updateTrainers_success_returns200() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setUsername("John.Smith");
        request.setTrainerUsernames(List.of("Jane.Doe"));

        Trainer trainer = new Trainer();
        User trainerUser = new User();
        trainerUser.setUsername("Jane.Doe");
        trainer.setUser(trainerUser);

        TrainerSummaryResponse summary = new TrainerSummaryResponse("Jane.Doe", "Jane", "Doe", 1L);

        when(trainerService.getTrainer("Jane.Doe")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toTrainerSummary(trainer)).thenReturn(summary);

        mockMvc.perform(put("/api/trainee/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Jane.Doe"));
    }

    @Test
    @DisplayName("PUT /api/trainee/trainers: 404 when trainer not found")
    void updateTrainers_trainerNotFound_returns404() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setUsername("John.Smith");
        request.setTrainerUsernames(List.of("unknown"));

        when(trainerService.getTrainer("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/trainee/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/trainee/trainings: 200 with training list")
    void getTrainings_returns200() throws Exception {
        TraineeTrainingsRequest request = new TraineeTrainingsRequest();
        request.setUsername("John.Smith");

        when(traineeService.getTrainings("John.Smith", null, null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainee/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PATCH /api/trainee/activate: 200 on status change")
    void activateDeactivate_success_returns200() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("John.Smith");
        request.setIsActive(false);

        when(traineeService.getTrainee("John.Smith")).thenReturn(Optional.of(trainee));

        mockMvc.perform(patch("/api/trainee/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).activateDeactivate("John.Smith", false);
    }

    @Test
    @DisplayName("PATCH /api/trainee/activate: 409 when already in requested state")
    void activateDeactivate_alreadyInState_returns409() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("John.Smith");
        request.setIsActive(true);

        when(traineeService.getTrainee("John.Smith")).thenReturn(Optional.of(trainee));

        mockMvc.perform(patch("/api/trainee/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /api/trainee/activate: 404 when trainee not found")
    void activateDeactivate_notFound_returns404() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("unknown");
        request.setIsActive(true);

        when(traineeService.getTrainee("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/trainee/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/trainee/activate: 400 when isActive is null")
    void activateDeactivate_nullIsActive_returns400() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest();
        request.setUsername("John.Smith");
        request.setIsActive(null);

        mockMvc.perform(patch("/api/trainee/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
