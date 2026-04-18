package com.gym.mapper.impl;

import com.gym.dto.request.TraineeRegistrationRequest;
import com.gym.dto.request.UpdateTraineeRequest;
import com.gym.dto.response.TraineeProfileResponse;
import com.gym.dto.response.TraineeTrainingResponse;
import com.gym.dto.response.TrainerSummaryResponse;
import com.gym.dto.response.UpdateTraineeResponse;
import com.gym.mapper.TraineeMapper;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TraineeMapperImpl implements TraineeMapper {

    public Trainee toEntity(TraineeRegistrationRequest request) {
        if (request == null) {
            return null;
        }

        Trainee trainee = new Trainee();
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        trainee.setUser(user);
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        return trainee;
    }

    public Trainee toUpdatedEntity(Trainee trainee, UpdateTraineeRequest request) {
        if (request == null) {
            return null;
        }

        trainee.getUser().setUsername(request.getUsername());
        trainee.getUser().setFirstName(request.getFirstName());
        trainee.getUser().setLastName(request.getLastName());
        trainee.getUser().setActive(request.getIsActive());
        trainee.setUser(trainee.getUser());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());

        return trainee;
    }

    public TraineeProfileResponse toProfileResponse(Trainee trainee) {
        if (trainee == null) {
            return null;
        }
        return new TraineeProfileResponse(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainerSummaries(trainee)
        );
    }

    public UpdateTraineeResponse toUpdateResponse(Trainee trainee) {
        if (trainee == null) {
            return null;
        }
        return new UpdateTraineeResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainerSummaries(trainee)

        );
    }

    public TraineeTrainingResponse toTraineeTrainingResponse(Training training) {
        if (training == null) {
            return null;
        }
        return new TraineeTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                training.getTrainer().getUser().getFirstName() + " "
                        + training.getTrainer().getUser().getLastName());
    }

    private List<TrainerSummaryResponse> trainerSummaries(Trainee trainee) {
        List<Trainer> trainers = trainee.getTrainers();
        return trainers.stream()
                .map(trainer -> new TrainerSummaryResponse(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization().getId()
                ))
                .toList();
    }
}
