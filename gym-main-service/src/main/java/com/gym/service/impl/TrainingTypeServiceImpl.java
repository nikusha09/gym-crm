package com.gym.service.impl;

import com.gym.model.TrainingType;
import com.gym.repository.TrainingTypeRepository;
import com.gym.service.TrainingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) { this.trainingTypeRepository = trainingTypeRepository; }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingType> findByName(String name) {
        return trainingTypeRepository.findByTrainingTypeName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> findAll() {
        return trainingTypeRepository.findAll();
    }
}
