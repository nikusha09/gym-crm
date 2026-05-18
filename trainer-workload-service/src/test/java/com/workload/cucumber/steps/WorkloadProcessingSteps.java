package com.workload.cucumber.steps;

import com.workload.dto.TrainerWorkloadRequest;
import com.workload.messaging.WorkloadMessageListener;
import com.workload.model.TrainerWorkload;
import com.workload.repository.TrainerWorkloadRepository;
import com.workload.service.TrainerWorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class WorkloadProcessingSteps {

    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Autowired
    private WorkloadMessageListener workloadMessageListener;

    private Exception thrownException;

    @Before
    public void cleanUp() {
        trainerWorkloadRepository.deleteAll();
        thrownException = null;
    }

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
        assertNotNull(trainerWorkloadService);
    }

    @Given("no workload exists for trainer {string}")
    public void noWorkloadExistsForTrainer(String username) {
        trainerWorkloadRepository.deleteAll();
        assertTrue(trainerWorkloadRepository.findByUsername(username).isEmpty());
    }

    @Given("a workload exists for trainer {string} in year {int} month {int} with duration {int}")
    public void aWorkloadExistsForTrainer(String username, int year, int month, int duration) {
        Optional<TrainerWorkload> existing = trainerWorkloadRepository.findByUsername(username);

        TrainerWorkload workload;
        if (existing.isPresent()) {
            workload = existing.get();
            TrainerWorkload.YearSummary yearSummary = workload.getYears().stream()
                    .filter(y -> y.getYear() == year)
                    .findFirst()
                    .orElseGet(() -> {
                        TrainerWorkload.YearSummary newYear = new TrainerWorkload.YearSummary();
                        newYear.setYear(year);
                        newYear.setMonths(new ArrayList<>());
                        workload.getYears().add(newYear);
                        return newYear;
                    });

            TrainerWorkload.MonthSummary monthSummary = yearSummary.getMonths().stream()
                    .filter(m -> m.getMonth() == month)
                    .findFirst()
                    .orElseGet(() -> {
                        TrainerWorkload.MonthSummary newMonth =
                                new TrainerWorkload.MonthSummary(month, 0);
                        yearSummary.getMonths().add(newMonth);
                        return newMonth;
                    });
            monthSummary.setTrainingsSummaryDuration(duration);
        } else {
            TrainerWorkload.MonthSummary monthSummary =
                    new TrainerWorkload.MonthSummary(month, duration);

            TrainerWorkload.YearSummary yearSummary = new TrainerWorkload.YearSummary();
            yearSummary.setYear(year);
            yearSummary.setMonths(new ArrayList<>());
            yearSummary.getMonths().add(monthSummary);

            workload = new TrainerWorkload();
            String[] parts = username.split("\\.");
            workload.setUsername(username);
            workload.setFirstName(parts[0]);
            workload.setLastName(parts[1]);
            workload.setActive(true);
            workload.setYears(new ArrayList<>());
            workload.getYears().add(yearSummary);
        }

        trainerWorkloadRepository.save(workload);
    }

    @When("a workload event is received for trainer {string} with firstName {string} lastName {string} duration {int} date {string} action {string}")
    public void aWorkloadEventIsReceived(String username, String firstName, String lastName,
                                         int duration, String date, String action) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName(firstName);
        request.setTrainerLastName(lastName);
        request.setTrainingDuration(duration);
        request.setTrainingDate(LocalDate.parse(date));
        request.setActionType(action);
        request.setActive(true);

        try {
            trainerWorkloadService.processWorkload(request);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @When("a workload event is received for trainer {string} with firstName {string} lastName {string} duration {int} date {string} action {string} and validation is applied")
    public void aWorkloadEventIsReceivedWithValidation(String username, String firstName, String lastName,
                                                       int duration, String date, String action) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName(firstName);
        request.setTrainerLastName(lastName);
        request.setTrainingDuration(duration);
        request.setTrainingDate(LocalDate.parse(date));
        request.setActionType(action);
        request.setActive(true);
        thrownException = workloadMessageListener.validateAndProcess(request);
    }

    @When("a workload event is received with missing username and firstName {string} lastName {string} duration {int} date {string} action {string}")
    public void aWorkloadEventIsReceivedWithMissingUsername(String firstName, String lastName,
                                                            int duration, String date, String action) {
        TrainerWorkloadRequest request = buildInvalidRequest(
                "", firstName, lastName, duration, date, action);
        thrownException = workloadMessageListener.validateAndProcess(request);
    }

    @When("a workload event is received for trainer {string} with missing firstName lastName {string} duration {int} date {string} action {string}")
    public void aWorkloadEventIsReceivedWithMissingFirstName(String username, String lastName,
                                                             int duration, String date, String action) {
        TrainerWorkloadRequest request = buildInvalidRequest(
                username, "", lastName, duration, date, action);
        thrownException = workloadMessageListener.validateAndProcess(request);
    }

    @When("a workload event is received for trainer {string} with firstName {string} missing lastName duration {int} date {string} action {string}")
    public void aWorkloadEventIsReceivedWithMissingLastName(String username, String firstName,
                                                            int duration, String date, String action) {
        TrainerWorkloadRequest request = buildInvalidRequest(
                username, firstName, "", duration, date, action);
        thrownException = workloadMessageListener.validateAndProcess(request);
    }

    @When("a workload event is received for trainer {string} with firstName {string} lastName {string} duration {int} date {string} with missing action")
    public void aWorkloadEventIsReceivedWithMissingAction(String username, String firstName,
                                                          String lastName, int duration, String date) {
        TrainerWorkloadRequest request = buildInvalidRequest(
                username, firstName, lastName, duration, date, "");
        thrownException = workloadMessageListener.validateAndProcess(request);
    }

    @When("a workload event is received for trainer {string} with firstName {string} lastName {string} duration {int} with missing date action {string}")
    public void aWorkloadEventIsReceivedWithMissingDate(String username, String firstName,
                                                        String lastName, int duration, String action) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName(firstName);
        request.setTrainerLastName(lastName);
        request.setTrainingDuration(duration);
        request.setTrainingDate(null);
        request.setActionType(action);
        request.setActive(true);
        thrownException = workloadMessageListener.validateAndProcess(request);
    }

    @Then("the workload for trainer {string} in year {int} month {int} should be {int}")
    public void theWorkloadShouldBe(String username, int year, int month, int expectedDuration) {
        TrainerWorkload workload = trainerWorkloadRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Workload not found for: " + username));

        int actualDuration = workload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Year not found: " + year))
                .getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Month not found: " + month))
                .getTrainingsSummaryDuration();

        assertEquals(expectedDuration, actualDuration);
    }

    @Then("the event should be rejected with validation error {string}")
    public void theEventShouldBeRejected(String expectedMessage) {
        assertNotNull(thrownException);
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    private TrainerWorkloadRequest buildInvalidRequest(String username, String firstName,
                                                       String lastName, int duration,
                                                       String date, String action) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(username);
        request.setTrainerFirstName(firstName);
        request.setTrainerLastName(lastName);
        request.setTrainingDuration(duration);
        request.setTrainingDate(LocalDate.parse(date));
        request.setActionType(action);
        request.setActive(true);
        return request;
    }
}
