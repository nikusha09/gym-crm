package com.gym.service.impl;

import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.repository.TraineeRepository;
import com.gym.service.TraineeService;
import com.gym.util.UsernamePasswordGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TraineeServiceImpl implements TraineeService {

    private TraineeRepository traineeRepository;
    private UsernamePasswordGenerator generator;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) { this.traineeRepository = traineeRepository; }

    @Autowired
    public void setGenerator(UsernamePasswordGenerator generator) { this.generator = generator; }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) { this.passwordEncoder = passwordEncoder; }

    @Override
    @Transactional
    public String createTrainee(Trainee trainee) {
        validateTraineeForCreate(trainee);
        String username = generator.generateUsername(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                traineeRepository.findAll()
                        .stream()
                        .map(t -> t.getUser())
                        .toList()
        );
        trainee.getUser().setUsername(username);

        String rawPassword = generator.generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        trainee.getUser().setPassword(encodedPassword);

        traineeRepository.save(trainee);

        log.info("Trainee created with username: {}", username);
        return rawPassword;
    }

    @Override
    @Transactional
    public void updateTrainee(Trainee trainee) {
        validateTraineeForUpdate(trainee);
        traineeRepository.save(trainee);
        log.info("Trainee updated: {}", trainee.getUser().getUsername());
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", username));
        traineeRepository.delete(trainee);
        log.info("Trainee deleted: {}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> getTrainee(String username) {
        log.debug("Fetching trainee: {}", username);
        return traineeRepository.findByUserUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees() {
        log.debug("Fetching all trainees");
        return traineeRepository.findAll();
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password must not be blank");
        }
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", username));
        trainee.getUser().setPassword(passwordEncoder.encode(newPassword));
        traineeRepository.save(trainee);
        log.info("Password changed for trainee: {}", username);
    }

    @Override
    @Transactional
    public void activateDeactivate(String username, Boolean isActive) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", username));
        trainee.getUser().setActive(isActive);
        traineeRepository.save(trainee);
        log.info("Trainee {} activation status changed to {}", username, isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainings(String username, LocalDate fromDate,
                                       LocalDate toDate, String trainerName, String trainingType) {
        log.debug("Fetching trainings for trainee: {}", username);
        return traineeRepository.findTrainings(username, fromDate, toDate, trainerName, trainingType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String username) {
        log.debug("Fetching unassigned trainers for trainee: {}", username);
        return traineeRepository.getUnassignedTrainers(username);
    }

    @Override
    @Transactional
    public void updateTrainers(String username, List<Trainer> trainers) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee", username));
        trainee.setTrainers(trainers);
        traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee: {}", username);
    }

    private void validateTraineeForCreate(Trainee trainee) {
        if (trainee.getUser() == null)
            throw new ValidationException("User must not be null");
        if (trainee.getUser().getFirstName() == null || trainee.getUser().getFirstName().isBlank())
            throw new ValidationException("First name is required");
        if (trainee.getUser().getLastName() == null || trainee.getUser().getLastName().isBlank())
            throw new ValidationException("Last name is required");
    }

    private void validateTraineeForUpdate(Trainee trainee) {
        validateTraineeForCreate(trainee);
        if (trainee.getId() == null)
            throw new ValidationException("Trainee id is required for update");
    }
}
