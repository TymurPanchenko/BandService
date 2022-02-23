package com.example.bandservice.service;

import com.example.bandservice.model.Band;

import java.util.List;

public interface BandService {
    Band create(Band band);
    Band readById(Integer id);
    Band readByName(String name);
    Band update(Integer id, Band band);
    void delete(Integer id);
    List<Band> getAll();
}
