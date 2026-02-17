package com.epam.gym.service;

import com.epam.gym.dao.TrainingDAO;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainingService {

    @Setter(onMethod_ = @Autowired)
    private TrainingDAO trainingDAO;

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate,
                                   Integer duration) {
        log.info("Creating training: {} for trainee {} and trainer {}",
                trainingName, traineeId, trainerId);

        Training training = Training.builder()
                .traineeId(traineeId)
                .trainerId(trainerId)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .build();

        return trainingDAO.save(training);
    }

    public Optional<Training> selectProfile(Long id) {
        return trainingDAO.findById(id);
    }

    public List<Training> findAll() {
        return trainingDAO.findAll();
    }
}
