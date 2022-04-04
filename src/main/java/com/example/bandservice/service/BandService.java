package com.example.bandservice.service;

import com.example.bandservice.model.Band;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface BandService {
    Band create(Band band);

    Band readById(Long id);

    Band readByName(String name);

    Band update(Long id, Band band);

    void delete(Long id);

    List<Band> getAll();

    Map<String, List<String>> getReport(HttpServletRequest request);

    List<String> getSingleReport(Long id, HttpServletRequest request);

    String getReadyCheck(Long id, HttpServletRequest request);

    boolean isTokenValidBoss(HttpServletRequest request);

    boolean isTokenValidBossAndUser(HttpServletRequest request);
}
