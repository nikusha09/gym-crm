package com.gym.mapper;

import com.gym.dto.request.TraineeRegistrationRequest;
import com.gym.dto.request.UpdateTraineeRequest;
import com.gym.dto.response.TraineeProfileResponse;
import com.gym.dto.response.TraineeTrainingResponse;
import com.gym.dto.response.UpdateTraineeResponse;
import com.gym.model.Trainee;
import com.gym.model.Training;

public interface TraineeMapper {
    Trainee toEntity(TraineeRegistrationRequest request);
    Trainee toUpdatedEntity(Trainee trainee, UpdateTraineeRequest request);
    TraineeProfileResponse toProfileResponse(Trainee trainee);
    UpdateTraineeResponse toUpdateResponse(Trainee trainee);
    TraineeTrainingResponse toTraineeTrainingResponse(Training training);
}
