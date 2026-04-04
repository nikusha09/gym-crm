package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateTraineeTrainersRequest {

    @NotBlank(message = "Trainee username is required")
    private String username;

    @NotNull(message = "Trainer usernames list is required")
    @NotEmpty(message = "Trainer usernames list must not be empty")
    private List<String> trainerUsernames;
}
