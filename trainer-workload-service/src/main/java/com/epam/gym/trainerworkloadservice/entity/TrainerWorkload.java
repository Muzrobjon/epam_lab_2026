package com.epam.gym.trainerworkloadservice.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workloads")
@CompoundIndexes({
       @CompoundIndex(name = "idx_trainer_full_name",
                def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Trainer username is required")
    @Field("trainerUsername")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    @Field("trainerFirstName")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    @Field("trainerLastName")
    private String trainerLastName;

    @NotNull(message = "Trainer status is required")
    @Field("trainerStatus")
    private Boolean trainerStatus;

    @Builder.Default
    @Field("years")
    private List<YearSummary> years = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummary {
        @NotNull
        private Integer year;

        @Builder.Default
        private List<MonthSummary> months = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        @NotNull
        private Integer month;

       @NotNull
        private Long trainingSummaryDuration;
    }
}