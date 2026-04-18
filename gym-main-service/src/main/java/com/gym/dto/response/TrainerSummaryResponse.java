package com.gym.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TrainerSummaryResponse {

    private String username;
    private String firstName;
    private String lastName;
    private Long specializationId;

}
