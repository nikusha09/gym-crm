package com.gym.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.cucumber.context.TestContext;
import com.gym.dto.request.TraineeRegistrationRequest;
import com.gym.dto.request.TrainerRegistrationRequest;
import com.gym.dto.request.UsernameRequest;
import com.gym.model.Trainee;
import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TraineeTrainerSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TestContext context;

    @Autowired
    private TrainerRepository trainerRepository;

    @Given("a trainee registration request with first name {string} and last name {string}")
    public void createTraineeRequest(String firstName, String lastName) {
        String username = firstName + "." + lastName;
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        context.addTraineeRequest(username, request);
    }

    @Given("a trainer registration request with first name {string} and last name {string} and specialization {string}")
    public void createTrainerRequest(String firstName, String lastName, String specializationId) {
        String username = firstName + "." + lastName;
        if (context.getTrainerRequest(username) != null) {
            username = username + "2";
        }
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setSpecializationId(Long.parseLong(specializationId));
        context.addTrainerRequest(username, request);
    }

    @When("the client sends trainee registration request for {string}")
    public void sendTraineeRequest(String username) throws Exception {
        TraineeRegistrationRequest request = context.getTraineeRequest(username);
        MvcResult result = mockMvc.perform(post("/api/trainee/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        context.addResult(username, result);

        if (result.getResponse().getStatus() == 201) {
            String password = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("password").asText();
            context.storePassword(username, password);
        }
    }

    @When("the client sends trainer registration request for {string}")
    public void sendTrainerRequest(String username) throws Exception {
        TrainerRegistrationRequest request = context.getTrainerRequest(username);
        MvcResult result = mockMvc.perform(post("/api/trainer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        context.addResult(username, result);

        if (result.getResponse().getStatus() == 201) {
            String password = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("password").asText();
            context.storePassword(username, password);
        }
    }

    @Then("the response status should be {int}")
    public void verifyStatus(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastResult().getResponse().getStatus());
    }

    @And("the trainee should be stored in database")
    public void verifyTraineeStoredInDb() {
        // verifies at least one trainee exists — extend with username param if needed
        assertFalse(traineeRepository.findAll().isEmpty());
    }

    @Then("the trainee {string} should have username {string}")
    public void verifyTraineeUsername(String key, String expectedUsername) {
        Optional<Trainee> trainee = traineeRepository.findByUserUsername(expectedUsername);
        assertTrue(trainee.isPresent());
    }

    @When("the client sends trainee registration request with missing first name")
    public void sendTraineeRequestWithMissingFirstName() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setLastName("Smith");

        MvcResult result = mockMvc.perform(post("/api/trainee/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        context.addResult("invalid-trainee", result);
    }

    @Then("the trainer {string} should exist in database")
    public void verifyTrainerUsername(String expectedUsername) {
        assertTrue(trainerRepository.findByUserUsername(expectedUsername).isPresent());
    }

    @And("the client gets trainee profile for {string}")
    public void getTraineeProfile(String username) throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername(username);

        MvcResult result = mockMvc.perform(get("/api/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + context.getLastToken())
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        context.addResult("profile:" + username, result);
    }

    @And("the response should contain first name {string} and last name {string}")
    public void verifyProfileName(String firstName, String lastName) throws Exception {
        String body = context.getLastResult().getResponse().getContentAsString();
        var json = objectMapper.readTree(body);
        assertEquals(firstName, json.get("firstName").asText());
        assertEquals(lastName, json.get("lastName").asText());
    }

    @And("the client gets trainer profile for {string}")
    public void getTrainerProfile(String username) throws Exception {
        UsernameRequest request = new UsernameRequest();
        request.setUsername(username);

        MvcResult result = mockMvc.perform(get("/api/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + context.getLastToken())
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        context.addResult("trainer-profile:" + username, result);
    }
}
