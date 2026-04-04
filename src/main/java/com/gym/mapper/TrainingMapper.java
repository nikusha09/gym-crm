package com.gym.mapper;

import com.gym.dto.request.AddTrainingRequest;
import com.gym.model.Training;

public interface TrainingMapper {
    Training toTraining(AddTrainingRequest request);
}
