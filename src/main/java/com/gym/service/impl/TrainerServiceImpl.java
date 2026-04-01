package com.gym.service.impl;

import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.repository.TrainerRepository;
import com.gym.service.TrainerService;
import com.gym.util.UsernamePasswordGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainerServiceImpl implements TrainerService {

    private TrainerRepository repository;
    private UsernamePasswordGenerator generator;

    @Autowired
    public void setRepository(TrainerRepository repository) { this.repository = repository; }

    @Autowired
    public void setGenerator(UsernamePasswordGenerator generator) { this.generator = generator; }

    @Override
    @Transactional
    public void createTrainer(Trainer trainer) {
        validateTrainerForCreate(trainer);
        String username = generator.generateUsername(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                repository.findAll()
                        .stream()
                        .map(Trainer::getUser)
                        .toList()
        );
        trainer.getUser().setUsername(username);
        trainer.getUser().setPassword(generator.generatePassword());
        repository.save(trainer);
        log.info("Trainer created with username: {}", username);
    }

    @Override
    @Transactional
    public void updateTrainer(Trainer trainer) {
        validateTrainerForUpdate(trainer);
        repository.save(trainer);
        log.info("Trainer updated: {}", trainer.getUser().getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainer(String username) {
        log.debug("Fetching trainer: {}", username);
        return repository.findByUserUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        log.debug("Fetching all trainers");
        return repository.findAll();
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank())
            throw new ValidationException("New password must not be blank");
        Trainer trainer = repository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", username));
        trainer.getUser().setPassword(newPassword);
        repository.save(trainer);
        log.info("Password changed for trainer: {}", username);
    }

    @Override
    @Transactional
    public void activateDeactivate(String username, Boolean isActive) {
        Trainer trainer = repository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", username));
        trainer.getUser().setActive(isActive);
        repository.save(trainer);
        log.info("Trainer {} activation status changed to {}", username, isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainings(String username, LocalDate fromDate,
                                       LocalDate toDate, String traineeName) {
        log.debug("Fetching trainings for trainer: {}", username);
        return repository.findTrainings(username, fromDate, toDate, traineeName);
    }

    private void validateTrainerForCreate(Trainer trainer) {
        if (trainer.getUser() == null)
            throw new ValidationException("User must not be null");
        if (trainer.getUser().getFirstName() == null || trainer.getUser().getFirstName().isBlank())
            throw new ValidationException("First name is required");
        if (trainer.getUser().getLastName() == null || trainer.getUser().getLastName().isBlank())
            throw new ValidationException("Last name is required");
        if (trainer.getSpecialization() == null)
            throw new ValidationException("Specialization is required");
    }

    private void validateTrainerForUpdate(Trainer trainer) {
        validateTrainerForCreate(trainer);
        if (trainer.getId() == null)
            throw new ValidationException("Trainer id is required for update");
    }
}
