package com.gym.controller;

import com.gym.dto.response.TrainingTypeResponse;
import com.gym.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training Types", description = "Training type reference data")
@Slf4j
public class TrainingTypeController {

    private TrainingTypeService trainingTypeService;

    @Autowired
    public void setTrainingTypeService(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @GetMapping
    @Operation(summary = "Get all training types")
    @ApiResponse(responseCode = "200", description = "Training types retrieved")
    public ResponseEntity<List<TrainingTypeResponse>> getAllTrainingTypes() {
        log.debug("Get all training types");

        List<TrainingTypeResponse> response = trainingTypeService.findAll()
                .stream()
                .map(trainingType -> new TrainingTypeResponse(
                        trainingType.getId(), trainingType.getTrainingTypeName()))
                .toList();

        return ResponseEntity.ok(response);
    }
}
