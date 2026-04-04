package com.gym.dto.response;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class UpdateTraineeResponse extends TraineeProfileResponse {

    private final String username;

    public UpdateTraineeResponse(String username,
                                 String firstName,
                                 String lastName,
                                 LocalDate dateOfBirth,
                                 String address,
                                 Boolean isActive,
                                 List<TrainerSummaryResponse> trainers) {
        super(firstName, lastName, dateOfBirth, address, isActive, trainers);
        this.username = username;
    }

}
