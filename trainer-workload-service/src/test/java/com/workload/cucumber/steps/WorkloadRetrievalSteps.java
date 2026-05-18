package com.workload.cucumber.steps;

import com.workload.model.TrainerWorkload;
import com.workload.service.TrainerWorkloadService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class WorkloadRetrievalSteps {

    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    private TrainerWorkload retrievedWorkload;

    private Exception thrownException;

    @When("the workload is requested for trainer {string}")
    public void theWorkloadIsRequestedForTrainer(String username) {
        try {
            retrievedWorkload = trainerWorkloadService.getWorkload(username);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Then("the response should contain trainer username {string}")
    public void theResponseShouldContainUsername(String username) {
        assertNotNull(retrievedWorkload);
        assertEquals(username, retrievedWorkload.getUsername());
    }

    @Then("the response should contain firstName {string}")
    public void theResponseShouldContainFirstName(String firstName) {
        assertNotNull(retrievedWorkload);
        assertEquals(firstName, retrievedWorkload.getFirstName());
    }

    @Then("the response should contain lastName {string}")
    public void theResponseShouldContainLastName(String lastName) {
        assertNotNull(retrievedWorkload);
        assertEquals(lastName, retrievedWorkload.getLastName());
    }

    @Then("a runtime exception should be thrown with message {string}")
    public void aRuntimeExceptionShouldBeThrown(String expectedMessage) {
        assertNotNull(thrownException);
        assertInstanceOf(RuntimeException.class, thrownException);
        assertEquals(expectedMessage, thrownException.getMessage());
    }
}
