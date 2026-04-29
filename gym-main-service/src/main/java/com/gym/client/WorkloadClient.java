package com.gym.client;

import com.gym.dto.request.WorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkloadClient {

    private final WebClient workloadWebClient;

    @CircuitBreaker(name = "workloadService", fallbackMethod = "fallback")
    public void sendWorkload(WorkloadRequest request, String jwtToken) {
        String transactionId = MDC.get("transactionId");

        workloadWebClient.post()
                .uri("/api/trainer-workload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .header("X-Transaction-Id", transactionId != null ? transactionId : "")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void fallback(WorkloadRequest request, String jwtToken, Throwable throwable) {
        log.warn("Workload service unavailable, skipping workload update. Reason: {}",
                throwable.getMessage());
    }
}
