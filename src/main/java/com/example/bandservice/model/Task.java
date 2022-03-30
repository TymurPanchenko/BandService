package com.example.bandservice.model;

import lombok.Data;

@Data
public class Task {
    private Long id;
    private String name;
    private String description;
    private boolean isCompleted;
    private Long bandId;
    private Long strength;
    private Long numberOfPeople;

    public Task() {
    }
}
