package com.epam.gym.trainerworkloadservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "training_months")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_month", nullable = false)
    private Integer month;

    @Column(name = "duration", nullable = false)
    private Long duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", nullable = false)
    private TrainingYear trainingYear;
}