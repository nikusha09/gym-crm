package com.gym.service.impl;

import com.gym.exception.AuthenticationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        User traineeUser = new User();
        traineeUser.setUsername("John.Smith");
        traineeUser.setPassword("correctPassword");

        trainee = new Trainee();
        trainee.setUser(traineeUser);

        User trainerUser = new User();
        trainerUser.setUsername("Jane.Doe");
        trainerUser.setPassword("correctPassword");

        trainer = new Trainer();
        trainer.setUser(trainerUser);
    }

    @Test
    @DisplayName("authenticate: trainee with correct password — succeeds")
    void authenticate_trainee_correctPassword_succeeds() {
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertDoesNotThrow(() -> authenticationService.authenticate("John.Smith", "correctPassword"));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    @DisplayName("authenticate: trainee with wrong password — throws AuthenticationException")
    void authenticate_trainee_wrongPassword_throwsAuthException() {
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("John.Smith", "wrongPassword"));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    @DisplayName("authenticate: trainer with correct password — succeeds")
    void authenticate_trainer_correctPassword_succeeds() {
        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.empty());
        when(trainerRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        assertDoesNotThrow(() -> authenticationService.authenticate("Jane.Doe", "correctPassword"));
    }

    @Test
    @DisplayName("authenticate: trainer with wrong password — throws AuthenticationException")
    void authenticate_trainer_wrongPassword_throwsAuthException() {
        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.empty());
        when(trainerRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("Jane.Doe", "wrongPassword"));
    }

    @Test
    @DisplayName("authenticate: user not found in either repository — throws AuthenticationException")
    void authenticate_userNotFound_throwsAuthException() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        when(trainerRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("unknown", "anyPassword"));
    }

    @Test
    @DisplayName("authenticate: user not found — trainer repository is still checked")
    void authenticate_userNotFound_bothRepositoriesQueried() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        when(trainerRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("unknown", "anyPassword"));

        verify(traineeRepository).findByUserUsername("unknown");
        verify(trainerRepository).findByUserUsername("unknown");
    }
}
