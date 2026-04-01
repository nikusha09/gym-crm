package com.gym.service.impl;

import com.gym.exception.ValidationException;
import com.gym.model.Training;
import com.gym.repository.TrainingRepository;
import com.gym.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {

    private TrainingRepository trainingRepository;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) { this.trainingRepository = trainingRepository; }

    @Override
    @Transactional
    public void addTraining(Training training) {
        validateTraining(training);
        trainingRepository.save(training);
        log.info("Training added: {}", training.getTrainingName());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Training> getTraining(Long id) {
        log.debug("Fetching training with id: {}", id);
        return trainingRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        log.debug("Fetching all trainings");
        return trainingRepository.findAll();
    }

    private void validateTraining(Training training) {
        if (training.getTrainingName() == null || training.getTrainingName().isBlank())
            throw new ValidationException("Training name is required");
        if (training.getTrainingType() == null)
            throw new ValidationException("Training type is required");
        if (training.getTrainingDate() == null)
            throw new ValidationException("Training date is required");
        if (training.getTrainingDuration() <= 0)
            throw new ValidationException("Training duration must be greater than zero");
        if (training.getTrainee() == null)
            throw new ValidationException("Trainee is required");
        if (training.getTrainer() == null)
            throw new ValidationException("Trainer is required");
    }
}
