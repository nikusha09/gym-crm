package com.workload.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trainer_workload")
@CompoundIndex(name = "first_last_name_idx", def = "{'firstName': 1, 'lastName': 1}")
public class TrainerWorkload {

    @Id
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private List<YearSummary> years = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummary {
        private int year;
        private List<MonthSummary> months = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        private int month;
        private int trainingsSummaryDuration;
    }
}
