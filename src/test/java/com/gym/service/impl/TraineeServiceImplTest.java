package com.gym.service.impl;

import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.User;
import com.gym.repository.TraineeRepository;
import com.gym.util.UsernamePasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UsernamePasswordGenerator generator;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setPassword("oldPassword");
        user.setActive(true);

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(user);
    }

    @Test
    @DisplayName("createTrainee: success — sets username and password, saves")
    void createTrainee_success() {
        when(traineeRepository.findAll()).thenReturn(List.of());
        when(generator.generateUsername("John", "Smith", List.of())).thenReturn("John.Smith");
        when(generator.generatePassword()).thenReturn("pass123456");

        traineeService.createTrainee(trainee);

        assertEquals("John.Smith", user.getUsername());
        assertEquals("pass123456", user.getPassword());
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("createTrainee: null User throws ValidationException")
    void createTrainee_nullUser_throwsValidation() {
        trainee.setUser(null);
        assertThrows(ValidationException.class, () -> traineeService.createTrainee(trainee));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    @DisplayName("createTrainee: blank firstName throws ValidationException")
    void createTrainee_blankFirstName_throwsValidation() {
        user.setFirstName("  ");
        assertThrows(ValidationException.class, () -> traineeService.createTrainee(trainee));
    }

    @Test
    @DisplayName("createTrainee: blank lastName throws ValidationException")
    void createTrainee_blankLastName_throwsValidation() {
        user.setLastName("");
        assertThrows(ValidationException.class, () -> traineeService.createTrainee(trainee));
    }

    @Test
    @DisplayName("updateTrainee: success — saves trainee")
    void updateTrainee_success() {
        traineeService.updateTrainee(trainee);
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("updateTrainee: null id throws ValidationException")
    void updateTrainee_nullId_throwsValidation() {
        trainee.setId(null);
        assertThrows(ValidationException.class, () -> traineeService.updateTrainee(trainee));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    @DisplayName("updateTrainee: null User throws ValidationException")
    void updateTrainee_nullUser_throwsValidation() {
        trainee.setUser(null);
        assertThrows(ValidationException.class, () -> traineeService.updateTrainee(trainee));
    }

    @Test
    @DisplayName("deleteByUsername: success — deletes found trainee")
    void deleteByUsername_success() {
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername("John.Smith");

        verify(traineeRepository).delete(trainee);
    }

    @Test
    @DisplayName("deleteByUsername: not found throws EntityNotFoundException")
    void deleteByUsername_notFound_throwsEntityNotFound() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.deleteByUsername("unknown"));
    }

    @Test
    @DisplayName("getTrainee: found — returns Optional with trainee")
    void getTrainee_found_returnsOptional() {
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getTrainee("John.Smith");

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    @DisplayName("getTrainee: not found — returns empty Optional")
    void getTrainee_notFound_returnsEmpty() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.getTrainee("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllTrainees: returns list from repository")
    void getAllTrainees_returnsList() {
        when(traineeRepository.findAll()).thenReturn(List.of(trainee));

        List<Trainee> result = traineeService.getAllTrainees();

        assertEquals(1, result.size());
        assertEquals(trainee, result.get(0));
    }

    @Test
    @DisplayName("changePassword: success — updates password and saves")
    void changePassword_success() {
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.changePassword("John.Smith", "oldPassword", "newPassword1");

        assertEquals("newPassword1", user.getPassword());
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("changePassword: blank newPassword throws ValidationException")
    void changePassword_blankNewPassword_throwsValidation() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword("John.Smith", "old", "  "));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    @DisplayName("changePassword: null newPassword throws ValidationException")
    void changePassword_nullNewPassword_throwsValidation() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword("John.Smith", "old", null));
    }

    @Test
    @DisplayName("changePassword: trainee not found throws EntityNotFoundException")
    void changePassword_notFound_throwsEntityNotFound() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> traineeService.changePassword("unknown", "old", "newPass"));
    }

    @Test
    @DisplayName("activateDeactivate: sets active status and saves")
    void activateDeactivate_setsStatusAndSaves() {
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.activateDeactivate("John.Smith", false);

        assertFalse(user.isActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("activateDeactivate: not found throws EntityNotFoundException")
    void activateDeactivate_notFound_throwsEntityNotFound() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> traineeService.activateDeactivate("unknown", true));
    }

    @Test
    @DisplayName("getTrainings: delegates to repository with all filters")
    void getTrainings_delegatesToRepository() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<Training> trainings = List.of(new Training());
        when(traineeRepository.findTrainings("John.Smith", from, to, "trainer1", "Yoga"))
                .thenReturn(trainings);

        List<Training> result = traineeService.getTrainings("John.Smith", from, to, "trainer1", "Yoga");

        assertEquals(trainings, result);
        verify(traineeRepository).findTrainings("John.Smith", from, to, "trainer1", "Yoga");
    }

    @Test
    @DisplayName("getUnassignedTrainers: delegates to repository")
    void getUnassignedTrainers_delegatesToRepository() {
        List<Trainer> trainers = List.of(new Trainer());
        when(traineeRepository.getUnassignedTrainers("John.Smith")).thenReturn(trainers);

        List<Trainer> result = traineeService.getUnassignedTrainers("John.Smith");

        assertEquals(trainers, result);
    }

    @Test
    @DisplayName("updateTrainers: replaces trainer list and saves")
    void updateTrainers_success() {
        List<Trainer> newTrainers = List.of(new Trainer());
        when(traineeRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.updateTrainers("John.Smith", newTrainers);

        assertEquals(newTrainers, trainee.getTrainers());
        verify(traineeRepository).save(trainee);
    }

    @Test
    @DisplayName("updateTrainers: not found throws EntityNotFoundException")
    void updateTrainers_notFound_throwsEntityNotFound() {
        when(traineeRepository.findByUserUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainers("unknown", List.of()));
    }
}
