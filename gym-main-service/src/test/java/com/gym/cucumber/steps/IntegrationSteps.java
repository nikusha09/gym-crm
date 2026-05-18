package com.gym.cucumber.steps;

import io.cucumber.java.en.Then;
import jakarta.jms.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationSteps {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${workout.queue.name}")
    private String workoutQueueName;

    @Then("a message should be sent to the workload queue")
    public void verifyMessageSentToQueue() throws Exception {
        jakarta.jms.Message message = jmsTemplate.receive(workoutQueueName);
        assertNotNull(message, "Expected a message on the workload queue but found none");
        String body = ((TextMessage) message).getText();
        assertNotNull(body);
        assertTrue(body.contains("Mike.Brown"));
    }

    @Then("the workload message should contain action type {string}")
    public void verifyMessageActionType(String actionType) throws Exception {
        jakarta.jms.Message message = jmsTemplate.receive(workoutQueueName);
        assertNotNull(message, "Expected a message on the workload queue but found none");
        String body = ((TextMessage) message).getText();
        assertTrue(body.contains(actionType));
    }

    @Then("the workload message should contain trainer username {string} and duration {int}")
    public void verifyMessageTrainerDetails(String trainerUsername, int duration) throws Exception {
        jakarta.jms.Message message = jmsTemplate.receive(workoutQueueName);
        assertNotNull(message, "Expected a message on the workload queue but found none");
        String body = ((TextMessage) message).getText();
        assertTrue(body.contains(trainerUsername));
        assertTrue(body.contains(String.valueOf(duration)));
    }
}
