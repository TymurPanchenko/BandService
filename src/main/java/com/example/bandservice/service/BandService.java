package com.example.bandservice.service;

import com.example.bandservice.model.Band;
import com.example.bandservice.model.BandDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface BandService {
    Band create(BandDTO band);

    Band readById(Long id);

    Object readByName(String name);

    Band update(Long id, BandDTO band);

    void delete(Long id);

    List<Band> getAll();

    Map<String, List<String>> getReport(HttpServletRequest request);

    List<String> getSingleReport(Long id, HttpServletRequest request);

    Boolean getReadyCheck(Long id, HttpServletRequest request);

    boolean isTokenValidBoss(HttpServletRequest request);

    boolean isTokenValidBossAndUser(HttpServletRequest request);
}
