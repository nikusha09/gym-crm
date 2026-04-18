package com.gym.service.impl;

import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.repository.TrainerRepository;
import com.gym.util.UsernamePasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository repository;

    @Mock
    private UsernamePasswordGenerator generator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setPassword("oldPassword");
        user.setActive(true);

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType("Yoga"));
    }

    @Test
    @DisplayName("createTrainer: success — sets username, encodes password, saves, returns raw password")
    void createTrainer_success() {
        when(repository.findAll()).thenReturn(List.of());
        when(generator.generateUsername("Jane", "Doe", List.of())).thenReturn("Jane.Doe");
        when(generator.generatePassword()).thenReturn("rawPassword");
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        String result = trainerService.createTrainer(trainer);

        assertEquals("rawPassword", result);
        assertEquals("Jane.Doe", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        verify(passwordEncoder).encode("rawPassword");
        verify(repository).save(trainer);
    }

    @Test
    @DisplayName("createTrainer: null User throws ValidationException")
    void createTrainer_nullUser_throwsValidation() {
        trainer.setUser(null);
        assertThrows(ValidationException.class, () -> trainerService.createTrainer(trainer));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("createTrainer: blank firstName throws ValidationException")
    void createTrainer_blankFirstName_throwsValidation() {
        user.setFirstName("  ");
        assertThrows(ValidationException.class, () -> trainerService.createTrainer(trainer));
    }

    @Test
    @DisplayName("createTrainer: blank lastName throws ValidationException")
    void createTrainer_blankLastName_throwsValidation() {
        user.setLastName("");
        assertThrows(ValidationException.class, () -> trainerService.createTrainer(trainer));
    }

    @Test
    @DisplayName("createTrainer: null specialization throws ValidationException")
    void createTrainer_nullSpecialization_throwsValidation() {
        trainer.setSpecialization(null);
        assertThrows(ValidationException.class, () -> trainerService.createTrainer(trainer));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("updateTrainer: success — saves trainer")
    void updateTrainer_success() {
        trainerService.updateTrainer(trainer);
        verify(repository).save(trainer);
    }

    @Test
    @DisplayName("updateTrainer: null id throws ValidationException")
    void updateTrainer_nullId_throwsValidation() {
        trainer.setId(null);
        assertThrows(ValidationException.class, () -> trainerService.updateTrainer(trainer));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("updateTrainer: null specialization throws ValidationException")
    void updateTrainer_nullSpecialization_throwsValidation() {
        trainer.setSpecialization(null);
        assertThrows(ValidationException.class, () -> trainerService.updateTrainer(trainer));
    }

    @Test
    @DisplayName("getTrainer: found — returns Optional with trainer")
    void getTrainer_found_returnsOptional() {
        when(repository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.getTrainer("Jane.Doe");

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    @DisplayName("getTrainer: not found — returns empty Optional")
    void getTrainer_notFound_returnsEmpty() {
        when(repository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        assertTrue(trainerService.getTrainer("unknown").isEmpty());
    }

    @Test
    @DisplayName("getAllTrainers: returns list from repository")
    void getAllTrainers_returnsList() {
        when(repository.findAll()).thenReturn(List.of(trainer));

        List<Trainer> result = trainerService.getAllTrainers();

        assertEquals(1, result.size());
        assertEquals(trainer, result.get(0));
    }

    @Test
    @DisplayName("changePassword: success — encodes new password and saves")
    void changePassword_success() {
        when(repository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.encode("newPassword1")).thenReturn("encodedNewPassword");

        trainerService.changePassword("Jane.Doe", "oldPassword", "newPassword1");

        assertEquals("encodedNewPassword", user.getPassword());
        verify(passwordEncoder).encode("newPassword1");
        verify(repository).save(trainer);
    }

    @Test
    @DisplayName("changePassword: blank newPassword throws ValidationException")
    void changePassword_blankNewPassword_throwsValidation() {
        assertThrows(ValidationException.class,
                () -> trainerService.changePassword("Jane.Doe", "old", "  "));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("changePassword: null newPassword throws ValidationException")
    void changePassword_nullNewPassword_throwsValidation() {
        assertThrows(ValidationException.class,
                () -> trainerService.changePassword("Jane.Doe", "old", null));
    }

    @Test
    @DisplayName("changePassword: trainer not found throws EntityNotFoundException")
    void changePassword_notFound_throwsEntityNotFound() {
        when(repository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> trainerService.changePassword("unknown", "old", "newPass"));
    }

    @Test
    @DisplayName("activateDeactivate: sets active status and saves")
    void activateDeactivate_setsStatusAndSaves() {
        when(repository.findByUserUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        trainerService.activateDeactivate("Jane.Doe", false);

        assertFalse(user.isActive());
        verify(repository).save(trainer);
    }

    @Test
    @DisplayName("activateDeactivate: not found throws EntityNotFoundException")
    void activateDeactivate_notFound_throwsEntityNotFound() {
        when(repository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> trainerService.activateDeactivate("unknown", true));
    }

    @Test
    @DisplayName("getTrainings: delegates to repository with all filters")
    void getTrainings_delegatesToRepository() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<Training> trainings = List.of(new Training());
        when(repository.findTrainings("Jane.Doe", from, to, "trainee1")).thenReturn(trainings);

        List<Training> result = trainerService.getTrainings("Jane.Doe", from, to, "trainee1");

        assertEquals(trainings, result);
        verify(repository).findTrainings("Jane.Doe", from, to, "trainee1");
    }
}
