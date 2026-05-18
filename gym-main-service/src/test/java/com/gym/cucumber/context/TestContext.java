package com.gym.cucumber.context;

import com.gym.dto.request.TraineeRegistrationRequest;
import com.gym.dto.request.TrainerRegistrationRequest;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestContext {

    private final Map<String, TraineeRegistrationRequest> traineeRequests = new HashMap<>();
    private final Map<String, TrainerRegistrationRequest> trainerRequests = new HashMap<>();
    private final Map<String, String> passwords = new HashMap<>();
    private final Map<String, MvcResult> results = new HashMap<>();
    @Getter
    private MvcResult lastResult;
    private final Map<String, String> tokens = new HashMap<>();
    @Getter
    private String lastToken;

    public void addTraineeRequest(String username, TraineeRegistrationRequest request) {
        traineeRequests.put(username, request);
    }

    public TraineeRegistrationRequest getTraineeRequest(String username) {
        return traineeRequests.get(username);
    }

    public void addTrainerRequest(String username, TrainerRegistrationRequest request) {
        trainerRequests.put(username, request);
    }

    public TrainerRegistrationRequest getTrainerRequest(String username) {
        return trainerRequests.get(username);
    }

    public void storePassword(String username, String password) {
        passwords.put(username, password);
    }

    public String getPassword(String username) {
        return passwords.get(username);
    }

    public void addResult(String key, MvcResult result) {
        results.put(key, result);
        lastResult = result;
    }

    public MvcResult getResult(String key) {
        return results.get(key);
    }

    public void storeToken(String username, String token) {
        tokens.put(username, token);
        lastToken = token;
    }

    public String getToken(String username) {
        return tokens.get(username);
    }

    public void clear() {
        traineeRequests.clear();
        trainerRequests.clear();
        passwords.clear();
        results.clear();
        lastResult = null;
        tokens.clear();
    }
}
