package com.epam.gym.service;

import com.epam.gym.dao.TrainerDAO;
import com.epam.gym.model.Trainer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainerService {

    @Setter(onMethod_ = @Autowired)
    private TrainerDAO trainerDAO;

    public Trainer createProfile(String firstName, String lastName, String specialization) {
        log.info("Creating trainer profile for {} {}", firstName, lastName);

        Trainer trainer = Trainer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .userName(generateUsername(firstName, lastName))
                .isActive(true)
                .specialization(specialization)
                .build();

        return trainerDAO.save(trainer);
    }

    public Trainer updateProfile(Trainer trainer) {
        log.info("Updating trainer profile: {}", trainer.getUserName());
        if (!trainerDAO.exists(trainer.getUserId())) {
            throw new RuntimeException("Trainer not found: " + trainer.getUserId());
        }
        return trainerDAO.save(trainer);
    }

    public Optional<Trainer> selectProfile(Long id) {
        return trainerDAO.findById(id);
    }

    public List<Trainer> findAll() {
        return trainerDAO.findAll();
    }

    private String generateUsername(String firstName, String lastName) {
        return (firstName + "." + lastName).toLowerCase();
    }
}
