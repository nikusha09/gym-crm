package com.gym.service;

import com.gym.model.Trainer;
import com.gym.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerService {
    void createTrainer(Trainer trainer);
    void updateTrainer(Trainer trainer);
    Optional<Trainer> getTrainer(String username);
    List<Trainer> getAllTrainers();
    void changePassword(String username, String oldPassword, String newPassword);
    void activateDeactivate(String username, Boolean isActive);
    List<Training> getTrainings(String username, LocalDate fromDate, LocalDate toDate, String traineeName);
}
