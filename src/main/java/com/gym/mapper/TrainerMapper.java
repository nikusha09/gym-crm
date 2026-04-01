package com.gym.mapper;

import com.gym.dto.request.TrainerRegistrationRequest;
import com.gym.dto.request.UpdateTrainerRequest;
import com.gym.dto.response.TrainerProfileResponse;
import com.gym.dto.response.TrainerSummaryResponse;
import com.gym.dto.response.TrainerTrainingResponse;
import com.gym.dto.response.UpdateTrainerResponse;
import com.gym.model.Trainer;
import com.gym.model.Training;

public interface TrainerMapper {
    Trainer toEntity(TrainerRegistrationRequest request);
    TrainerSummaryResponse toTrainerSummary(Trainer trainer);
    TrainerProfileResponse toProfileResponse(Trainer trainer);
    Trainer toUpdatedEntity(Trainer trainer, UpdateTrainerRequest request);
    UpdateTrainerResponse toUpdateResponse(Trainer trainer);
    TrainerTrainingResponse toTrainerTrainingResponse(Training training);
}
