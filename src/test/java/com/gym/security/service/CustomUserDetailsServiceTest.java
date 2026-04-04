package com.gym.security.service;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        User traineeUser = new User();
        traineeUser.setUsername("John.Smith");
        traineeUser.setPassword("encodedPassword");
        traineeUser.setActive(true);
        trainee = new Trainee();
        trainee.setUser(traineeUser);

        User trainerUser = new User();
        trainerUser.setUsername("Jane.Doe");
        trainerUser.setPassword("encodedPassword");
        trainerUser.setActive(true);
        trainer = new Trainer();
        trainer.setUser(trainerUser);
    }

    @Test
    @DisplayName("loadUserByUsername: found as trainee — returns UserDetails")
    void loadUserByUsername_foundAsTrainee_returnsUserDetails() {
        when(traineeRepository.findByUserUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        UserDetails result = customUserDetailsService.loadUserByUsername("John.Smith");

        assertEquals("John.Smith", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verifyNoInteractions(trainerRepository);
    }

    @Test
    @DisplayName("loadUserByUsername: not trainee, found as trainer — returns UserDetails")
    void loadUserByUsername_foundAsTrainer_returnsUserDetails() {
        when(traineeRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.empty());
        when(trainerRepository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        UserDetails result = customUserDetailsService.loadUserByUsername("Jane.Doe");

        assertEquals("Jane.Doe", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
    }

    @Test
    @DisplayName("loadUserByUsername: not found in either repo — throws UsernameNotFoundException")
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        when(trainerRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknown"));
    }

    @Test
    @DisplayName("loadUserByUsername: trainee found — trainer repo never queried")
    void loadUserByUsername_traineeFound_trainerRepoNotQueried() {
        when(traineeRepository.findByUserUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        customUserDetailsService.loadUserByUsername("John.Smith");

        verifyNoInteractions(trainerRepository);
    }
}
