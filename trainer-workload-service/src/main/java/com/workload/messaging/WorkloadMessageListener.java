package com.workload.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workload.dto.TrainerWorkloadRequest;
import com.workload.service.TrainerWorkloadService;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageListener {

    private final TrainerWorkloadService trainerWorkloadService;
    private final ObjectMapper objectMapper;
    private final JmsTemplate jmsTemplate;

    @Value("${workout.queue.name}")
    private String workoutQueueName;

    @Value("${workout.dlq.name}")
    private String dlqName;

    @JmsListener(destination = "${workout.queue.name}")
    public void onMessage(Message message) {
        String transactionId = null;
        try {
            transactionId = message.getStringProperty("X-Transaction-Id");
            if (transactionId == null || transactionId.isBlank()) {
                transactionId = java.util.UUID.randomUUID().toString();
            }
            MDC.put("transactionId", transactionId);

            log.info("Received workload message | transactionId={}", transactionId);

            String json = ((TextMessage) message).getText();
            TrainerWorkloadRequest request = objectMapper.readValue(json, TrainerWorkloadRequest.class);

            validateRequest(request);

            trainerWorkloadService.processWorkload(request);

            log.info("Workload message processed successfully | trainer={} action={}",
                    request.getTrainerUsername(), request.getActionType());

        } catch (IllegalArgumentException e) {
            log.error("Invalid workload message, routing to DLQ | reason={} transactionId={}",
                    e.getMessage(), transactionId);
            routeToDlq(message, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to process workload message | reason={} transactionId={}",
                    e.getMessage(), transactionId);
            routeToDlq(message, e.getMessage());
        } finally {
            MDC.clear();
        }
    }

    private void validateRequest(TrainerWorkloadRequest request) {
        if (request.getTrainerUsername() == null || request.getTrainerUsername().isBlank()) {
            throw new IllegalArgumentException("Trainer username is missing");
        }
        if (request.getTrainerFirstName() == null || request.getTrainerFirstName().isBlank()) {
            throw new IllegalArgumentException("Trainer first name is missing");
        }
        if (request.getTrainerLastName() == null || request.getTrainerLastName().isBlank()) {
            throw new IllegalArgumentException("Trainer last name is missing");
        }
        if (request.getTrainingDate() == null) {
            throw new IllegalArgumentException("Training date is missing");
        }
        if (request.getTrainingDuration() <= 0) {
            throw new IllegalArgumentException("Training duration is invalid");
        }
        if (request.getActionType() == null || request.getActionType().isBlank()) {
            throw new IllegalArgumentException("Action type is missing");
        }
    }

    private void routeToDlq(Message originalMessage, String reason) {
        try {
            jmsTemplate.send(dlqName, session -> {
                TextMessage dlqMessage = session.createTextMessage(
                        ((TextMessage) originalMessage).getText());
                dlqMessage.setStringProperty("X-Transaction-Id",
                        originalMessage.getStringProperty("X-Transaction-Id"));
                dlqMessage.setStringProperty("DLQ-Reason", reason);
                return dlqMessage;
            });
            log.info("Message routed to DLQ | reason={}", reason);
        } catch (Exception e) {
            log.error("Failed to route message to DLQ | reason={}", e.getMessage());
        }
    }
}
