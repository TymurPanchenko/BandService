package com.example.bandservice.controller;

import com.example.bandservice.exception.NullBandReferenceException;
import com.example.bandservice.model.Band;
import com.example.bandservice.service.BandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/bands")
public class BandController {
    private final BandService bandService;
    private final Logger logger = LoggerFactory.getLogger(BandController.class);

    public BandController(BandService bandService) {
        this.bandService = bandService;
    }

    @PostMapping
    public ResponseEntity<Band> saveBand(@Valid @RequestBody Band band, Errors errors, HttpServletRequest request) {
        bandService.isTokenValidBoss(request);
        logger.info("Creating new band");
        if (errors.hasErrors()) {
            throw new NullBandReferenceException("Band is not valid");
        }
        Band band2 = bandService.readByName(band.getName());
        if (band2 != null) {
            throw new NullBandReferenceException("The band is in DB");
        }
        return ResponseEntity.ok(bandService.create(band));
    }

    @GetMapping
    public ResponseEntity<?> getBand(@RequestParam(value = "bandName", required = false) String name, HttpServletRequest request) {

        bandService.isTokenValidBoss(request);
        if (name == null) {
            logger.info("Getting all bands");
            return ResponseEntity.ok(bandService.getAll());
        } else {
            logger.info("Getting band name = {}", name);
            return ResponseEntity.ok(bandService.readByName(name));
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<Band> findBandById(@PathVariable("id") Long id, HttpServletRequest request) {
        bandService.isTokenValidBossAndUser(request);
        logger.info("Getting band id = {}", id);
        Band band = bandService.readById(id);
        if (Objects.isNull(band)) {
            throw new NullBandReferenceException("Not found");
        } else {
            return ResponseEntity.ok(band);
        }
    }

    @GetMapping("/report")
    public ResponseEntity<Map<String, List<String>>> getReport(HttpServletRequest request) {
        bandService.isTokenValidBoss(request);
        logger.info("Getting global report");
        return ResponseEntity.ok(bandService.getReport(request));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<List<String>> getBandReport(@PathVariable("id") Long id, HttpServletRequest request) {
        bandService.isTokenValidBoss(request);
        logger.info("Getting band report with id {}", id);
        return ResponseEntity.ok(bandService.getSingleReport(id, request));
    }

    @GetMapping("/tasks/{id}/check")
    public ResponseEntity<String> makeReadyCheck(@PathVariable("id") Long id, HttpServletRequest request) {
        bandService.isTokenValidBoss(request);
        logger.info("Checking task with id {}", id);
        return ResponseEntity.ok(bandService.getReadyCheck(id, request));
    }

    @DeleteMapping("/{id}")
    public void deleteBand(@PathVariable("id") Long id, HttpServletRequest request) {
        bandService.isTokenValidBoss(request);
        logger.info("Deleting band id = {}", id);
        bandService.delete(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Band> updateBand(@PathVariable("id") Long id, @RequestBody Band band, HttpServletRequest request) {
        bandService.isTokenValidBoss(request);
        logger.info("Updating band id = {}", id);
        return ResponseEntity.ok(bandService.update(id, band));
    }
}
