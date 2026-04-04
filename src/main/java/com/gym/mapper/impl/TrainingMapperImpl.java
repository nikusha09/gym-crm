package com.gym.mapper.impl;

import com.gym.dto.request.AddTrainingRequest;
import com.gym.exception.EntityNotFoundException;
import com.gym.mapper.TrainingMapper;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import com.gym.service.TraineeService;
import com.gym.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapperImpl implements TrainingMapper {

    private TraineeService traineeService;
    private TrainerService trainerService;

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Override
    public Training toTraining(AddTrainingRequest request) {

        Trainee trainee = traineeService.getTrainee(request.getTraineeUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainee", request.getTraineeUsername()));

        Trainer trainer = trainerService.getTrainer(request.getTrainerUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer", request.getTrainerUsername()));

        TrainingType trainingType = trainer.getSpecialization();

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(request.getTrainingName());
        training.setTrainingType(trainingType);
        training.setTrainingDate(request.getTrainingDate());
        training.setTrainingDuration(request.getTrainingDuration());

        return training;
    }
}
