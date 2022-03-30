package com.example.bandservice.model;

import lombok.Data;

@Data
public class User {
    private Long userId;
    private Long bandId;
    private Long taskId;
    private String name;

    public User() {
    }
}