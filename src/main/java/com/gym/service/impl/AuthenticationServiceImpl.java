package com.gym.service.impl;

import com.gym.exception.AuthenticationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import com.gym.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);

        Optional<Trainee> trainee = traineeRepository.findByUserUsername(username);
        if (trainee.isPresent()) {
            if (!trainee.get().getUser().getPassword().equals(password)) {
                log.warn("Authentication failed for trainee username: {}", username);
                throw new AuthenticationException(username);
            }
            log.debug("Authentication successful for username: {}", username);
            return;
        }

        Optional<Trainer> trainer = trainerRepository.findByUserUsername(username);
        if (trainer.isPresent()) {
            if (!trainer.get().getUser().getPassword().equals(password)) {
                log.warn("Authentication failed for trainer username: {}", username);
                throw new AuthenticationException(username);
            }
            log.debug("Authentication successful for username: {}", username);
            return;
        }

        log.warn("Authentication failed — user not found: {}", username);
        throw new AuthenticationException(username);
    }
}
