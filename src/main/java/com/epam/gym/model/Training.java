package com.epam.gym.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Training {
    private Long trainingId;
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    //TODO:
    // Is it in minutes, hours, seconds, etc.? You can for example leave a comment or give a field more descriptive name
    private Integer trainingDuration;
}

