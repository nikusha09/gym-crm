package com.gym.mapper.impl;

import com.gym.dto.request.TrainerRegistrationRequest;
import com.gym.dto.request.UpdateTrainerRequest;
import com.gym.dto.response.*;
import com.gym.mapper.TrainerMapper;
import com.gym.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrainerMapperImpl implements TrainerMapper {

    public Trainer toEntity(TrainerRegistrationRequest request) {
        if (request == null) {
            return null;
        }

        Trainer trainer = new Trainer();
        User user = new User();
        TrainingType trainingType = new TrainingType();
        trainingType.setId(request.getSpecializationId());

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);

        return trainer;
    }

    public Trainer toUpdatedEntity(Trainer trainer, UpdateTrainerRequest request) {
        if (request == null) {
            return null;
        }

        trainer.getUser().setUsername(request.getUsername());
        trainer.getUser().setFirstName(request.getFirstName());
        trainer.getUser().setLastName(request.getLastName());
        trainer.getUser().setActive(request.getIsActive());
        trainer.setUser(trainer.getUser());

        return trainer;
    }

    public UpdateTrainerResponse toUpdateResponse(Trainer trainer) {
        if (trainer == null) {
            return null;
        }
        return new UpdateTrainerResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getId(),
                trainer.getUser().isActive(),
                traineeSummaries(trainer)
        );
    }

    public TrainerProfileResponse toProfileResponse(Trainer trainer) {
        if (trainer == null) {
            return null;
        }
        return new TrainerProfileResponse(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getId(),
                trainer.getUser().isActive(),
                traineeSummaries(trainer)
        );
    }

    public TrainerSummaryResponse toTrainerSummary(Trainer trainer) {
        return new TrainerSummaryResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getId());
    }

    public TrainerTrainingResponse toTrainerTrainingResponse(Training training) {
        if (training == null) {
            return null;
        }
        return new TrainerTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                training.getTrainee().getUser().getFirstName() + " "
                        + training.getTrainee().getUser().getLastName()
        );
    }

    private List<TraineeSummaryResponse> traineeSummaries(Trainer trainer) {
        List<Trainee> trainees = trainer.getTrainees();
        return trainees.stream()
                .map(trainee -> new TraineeSummaryResponse(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .toList();
    }
}
