package com.gym.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.cucumber.context.TestContext;
import com.gym.dto.request.AddTrainingRequest;
import com.gym.model.Training;
import com.gym.repository.TrainingRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TrainingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestContext context;

    @Autowired
    private TrainingRepository trainingRepository;

    @When("the client adds training with trainee {string}, trainer {string}, name {string}, duration {int}")
    public void addTraining(String trainee, String trainer, String name, int duration) throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername(trainee);
        request.setTrainerUsername(trainer);
        request.setTrainingName(name);
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(duration);

        String token = context.getLastToken();

        MvcResult result = mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        context.addResult("training:" + name, result);
    }

    @Then("the training should be stored in database")
    public void verifyTrainingStored() {
        Optional<Training> training = trainingRepository.findAll().stream().findFirst();
        assertTrue(training.isPresent());
    }

    @And("the client adds training without token with trainee {string}, trainer {string}, name {string}, duration {int}")
    public void addTrainingWithoutToken(String trainee, String trainer, String name, int duration) throws Exception {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername(trainee);
        request.setTrainerUsername(trainer);
        request.setTrainingName(name);
        request.setTrainingDate(LocalDate.now());
        request.setTrainingDuration(duration);

        MvcResult result = mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        context.addResult("training-no-token", result);
    }

    @And("the client deletes the training")
    public void deleteTraining() throws Exception {
        Training training = trainingRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No training found"));

        MvcResult result = mockMvc.perform(delete("/api/trainings/" + training.getId())
                        .header("Authorization", "Bearer " + context.getLastToken()))
                .andReturn();

        context.addResult("delete-training", result);
    }
}
