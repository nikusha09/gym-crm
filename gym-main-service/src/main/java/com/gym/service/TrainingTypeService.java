package com.gym.service;

import com.gym.model.TrainingType;
import java.util.List;
import java.util.Optional;

public interface TrainingTypeService {
    Optional<TrainingType> findByName(String name);
    List<TrainingType> findAll();
}
