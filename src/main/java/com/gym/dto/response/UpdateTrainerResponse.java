package com.gym.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateTrainerResponse extends TrainerProfileResponse {

    private final String username;

    public UpdateTrainerResponse(String username,
                                 String firstName,
                                 String lastName,
                                 Long specializationId,
                                 Boolean isActive,
                                 List<TraineeSummaryResponse> trainees) {
        super(firstName, lastName, specializationId, isActive, trainees);
        this.username = username;
    }
}
