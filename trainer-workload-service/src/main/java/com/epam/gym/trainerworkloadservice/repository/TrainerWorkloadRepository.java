package com.epam.gym.trainerworkloadservice.repository;

import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);

    List<TrainerWorkload> findByTrainerFirstNameAndTrainerLastName(String firstName, String lastName);

}
