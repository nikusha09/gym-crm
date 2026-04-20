package com.gym.service.impl;

import com.gym.client.WorkloadClient;
import com.gym.dto.request.WorkloadRequest;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.repository.TrainingRepository;
import com.gym.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {

    private TrainingRepository trainingRepository;
    private WorkloadClient workloadClient;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) { this.trainingRepository = trainingRepository; }

    @Autowired
    public void setWorkloadClient(WorkloadClient workloadClient) { this.workloadClient = workloadClient; }

    @Override
    @Transactional
    public void addTraining(Training training) {
        validateTraining(training);
        trainingRepository.save(training);

        String token = extractTokenFromSecurityContext();
        WorkloadRequest workloadRequest = buildWorkloadRequest(training, "ADD");
        workloadClient.sendWorkload(workloadRequest, token);

        log.info("Training added: {}", training.getTrainingName());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Training> getTraining(Long id) {
        log.debug("Fetching training with id: {}", id);
        return trainingRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getAllTrainings() {
        log.debug("Fetching all trainings");
        return trainingRepository.findAll();
    }

    @Override
    public void deleteTraining(Long trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Training", trainingId.toString()));

        String token = extractTokenFromSecurityContext();
        WorkloadRequest workloadRequest = buildWorkloadRequest(training, "DELETE");
        workloadClient.sendWorkload(workloadRequest, token);

        trainingRepository.delete(training);
    }

    private void validateTraining(Training training) {
        if (training.getTrainingName() == null || training.getTrainingName().isBlank())
            throw new ValidationException("Training name is required");
        if (training.getTrainingType() == null)
            throw new ValidationException("Training type is required");
        if (training.getTrainingDate() == null)
            throw new ValidationException("Training date is required");
        if (training.getTrainingDuration() <= 0)
            throw new ValidationException("Training duration must be greater than zero");
        if (training.getTrainee() == null)
            throw new ValidationException("Trainee is required");
        if (training.getTrainer() == null)
            throw new ValidationException("Trainer is required");
    }

    private WorkloadRequest buildWorkloadRequest(Training training, String actionType) {
        Trainer trainer = training.getTrainer();
        return new WorkloadRequest(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );
    }

    private String extractTokenFromSecurityContext() {
        try {
            jakarta.servlet.http.HttpServletRequest request =
                    ((jakarta.servlet.http.HttpServletRequest) org.springframework.web.context.request.RequestContextHolder
                            .currentRequestAttributes()
                            .resolveReference(org.springframework.web.context.request.RequestAttributes.REFERENCE_REQUEST));
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        } catch (Exception e) {
            log.warn("Could not extract JWT token from request");
        }
        return null;
    }
}
