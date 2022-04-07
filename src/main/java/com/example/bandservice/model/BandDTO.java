package com.example.bandservice.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BandDTO {
    @NotBlank
    private String name;
}
