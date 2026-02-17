package com.epam.gym.service;

import com.epam.gym.dao.TraineeDAO;
import com.epam.gym.model.Trainee;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TraineeService {

    @Setter(onMethod_ = @Autowired)
    private TraineeDAO traineeDAO;

    public Trainee createProfile(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        log.info("Creating trainee profile for {} {}", firstName, lastName);

        Trainee trainee = Trainee.builder()
                .firstName(firstName)
                .lastName(lastName)
                .userName(generateUsername(firstName, lastName))
                .isActive(true)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .build();

        return traineeDAO.save(trainee);
    }

    public Trainee updateProfile(Trainee trainee) {
        log.info("Updating trainee profile: {}", trainee.getUserName());
        if (!traineeDAO.exists(trainee.getUserId())) {
            throw new RuntimeException("Trainee not found: " + trainee.getUserId());
        }
        return traineeDAO.save(trainee);
    }

    public void deleteProfile(Long id) {
        log.info("Deleting trainee profile: {}", id);
        traineeDAO.delete(id);
    }

    public Optional<Trainee> selectProfile(Long id) {
        return traineeDAO.findById(id);
    }

    public List<Trainee> findAll() {
        return traineeDAO.findAll();
    }

    private String generateUsername(String firstName, String lastName) {
        return (firstName + "." + lastName).toLowerCase();
    }
}
