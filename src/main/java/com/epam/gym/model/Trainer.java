package com.epam.gym.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Trainer extends User {
    private String specialization;
    @Builder.Default
    private List<Training> trainings = new ArrayList<>();
}
