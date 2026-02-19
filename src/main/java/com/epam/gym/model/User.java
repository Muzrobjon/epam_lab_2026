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
    // TODO:
    //  Password field and generation logic are not implemented.
    //  We need to handle it according to the task requirements.
    private boolean isActive;
}

