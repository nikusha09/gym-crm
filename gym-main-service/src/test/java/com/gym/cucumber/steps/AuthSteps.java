package com.gym.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.cucumber.context.TestContext;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @When("the client logs in with username {string}")
    public void login(String username) throws Exception {
        String password = context.getPassword(username);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .header("Username", username)
                        .header("Password", password))
                .andReturn();

        context.addResult("login:" + username, result);

        if (result.getResponse().getStatus() == 200) {
            String token = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("token").asText();
            context.storeToken(username, token);
        }
    }

    @When("the client logs in with username {string} and wrong password {string}")
    public void loginWithWrongPassword(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .header("Username", username)
                        .header("Password", password))
                .andReturn();

        context.addResult("login:" + username, result);
    }
}
