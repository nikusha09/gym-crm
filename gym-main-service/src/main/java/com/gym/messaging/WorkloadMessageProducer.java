package com.gym.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.request.WorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WorkloadMessageProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${workout.queue.name}")
    private String workoutQueueName;

    public void sendWorkload(WorkloadRequest request) {
        String transactionId = MDC.get("transactionId");
        log.info("Sending workload message to queue | transactionId={} trainer={}",
                transactionId, request.getTrainerUsername());
        try {
            String jsonMessage = objectMapper.writeValueAsString(request);
            jmsTemplate.convertAndSend(workoutQueueName, jsonMessage, message -> {
                message.setStringProperty("X-Transaction-Id",
                        transactionId != null ? transactionId : "");
                return message;
            });
            log.info("Workload message sent successfully | trainer={}",
                    request.getTrainerUsername());
        } catch (Exception e) {
            log.error("Failed to serialize workload message | trainer={}",
                    request.getTrainerUsername());
        }
    }
}
