package com.epam.gym.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String firstName;
    private String lastName;
    private String userName;
    private boolean isActive;
}

