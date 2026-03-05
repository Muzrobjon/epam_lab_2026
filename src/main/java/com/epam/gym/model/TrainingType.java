package com.epam.gym.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "training_types")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long trainingTypeId;

    // TODO:
    //  From the task notes:
    //  12. Training Types table include constant list of values and could not be updated from the application.
    //  How can we achieve that?
    @NotBlank(message = "Training type name is required")
    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;
}