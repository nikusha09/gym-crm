package com.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateDeactivateRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "isActive field is required")
    private Boolean isActive;
}
