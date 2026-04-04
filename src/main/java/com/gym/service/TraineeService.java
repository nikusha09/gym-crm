package com.gym.service;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    void createTrainee(Trainee trainee);
    void updateTrainee(Trainee trainee);
    void deleteByUsername(String username);
    Optional<Trainee> getTrainee(String username);
    List<Trainee> getAllTrainees();
    void changePassword(String username, String oldPassword, String newPassword);
    void activateDeactivate(String username, Boolean isActive);
    List<Training> getTrainings(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingType);
    List<Trainer> getUnassignedTrainers(String username);
    void updateTrainers(String username, List<Trainer> trainers);
}
