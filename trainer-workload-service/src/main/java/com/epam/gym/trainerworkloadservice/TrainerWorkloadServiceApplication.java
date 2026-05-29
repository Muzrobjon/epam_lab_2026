package com.epam.gym.trainerworkloadservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;


@SpringBootApplication
@EnableJms
public class TrainerWorkloadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainerWorkloadServiceApplication.class, args);
    }

}
