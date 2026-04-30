package com.epam.gym.trainerworkloadservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadResponse {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean trainerStatus;
    private List<YearSummary> years;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummary {
        private Integer year;
        private List<MonthSummary> months;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        private Integer month;
        private Long trainingSummaryDuration;
    }
}