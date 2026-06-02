package com.gym.cucumber.hooks;

import com.gym.cucumber.context.TestContext;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Hooks {

    @Autowired
    private TestContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setup() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE trainee_trainer");
        jdbcTemplate.execute("TRUNCATE TABLE trainings");
        jdbcTemplate.execute("TRUNCATE TABLE trainees");
        jdbcTemplate.execute("TRUNCATE TABLE trainers");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        context.clear();
    }
}
