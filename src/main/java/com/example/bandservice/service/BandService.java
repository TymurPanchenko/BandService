package com.example.bandservice.service;

import com.example.bandservice.model.Band;

import java.util.List;
import java.util.Map;

public interface BandService {
    Band create(Band band);

    Band readById(Long id);

    Band readByName(String name);

    Band update(Long id, Band band);

    void delete(Long id);

    List<Band> getAll();

    Map<String, List<String>> getReport();

    List<String> getSingleReport(Long id);

    String getReadyCheck(Long id);
}
