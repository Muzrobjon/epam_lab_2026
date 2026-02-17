package com.epam.gym.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Trainee extends User {
    private LocalDate dateOfBirth;
    private String address;
    @Builder.Default
    private List<Training> trainings = new ArrayList<>();
}
