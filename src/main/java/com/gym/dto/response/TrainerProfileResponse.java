package com.gym.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class TrainerProfileResponse {

        private String firstName;
        private String lastName;
        private Long specializationId;
        private Boolean isActive;
        private List<TraineeSummaryResponse> trainees;

}
