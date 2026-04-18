package com.gym.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class TrainerTrainingResponse {

    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String traineeName;

}
