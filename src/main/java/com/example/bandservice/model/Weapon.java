package com.example.bandservice.model;

import lombok.Data;

@Data
public class Weapon {
    private Long id;
    private String name;
    private Integer damage;
    private Long task_id;
    private Long band_id;

    public Weapon() {
    }

}
