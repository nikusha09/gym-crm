package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TrainerTrainingsRequest {
    @NotBlank(message = "username is required")
    private String username;

    private LocalDate fromDate;
    private LocalDate toDate;
    private String traineeName;
}
