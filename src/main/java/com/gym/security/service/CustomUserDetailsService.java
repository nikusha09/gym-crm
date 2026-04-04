package com.gym.security.service;

import com.gym.model.User;
import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.debug("Loading user by username: {}", username);

        User user = traineeRepository.findByUserUsername(username)
                .map(trainee -> trainee.getUser())
                .orElseGet(() -> trainerRepository.findByUserUsername(username)
                        .map(trainer -> trainer.getUser())
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found: " + username)));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();
    }
}
