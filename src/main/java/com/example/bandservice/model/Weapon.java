package com.example.bandservice.model;

import lombok.Data;

@Data
public class Weapon {
    private Long id;
    private String name;
    private Integer damage;
    private Long taskId;
    private Long bandId;

    public Weapon() {
    }

}
