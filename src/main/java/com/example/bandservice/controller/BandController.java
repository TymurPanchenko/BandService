package com.example.bandservice.controller;

import com.example.bandservice.model.Band;
import com.example.bandservice.service.BandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/bands")
public class BandController {
    private final BandService bandService;

    public BandController(BandService bandService) {
        this.bandService = bandService;
    }

    @PostMapping
    public ResponseEntity<Band> saveBand(@Valid @RequestBody Band band) {
        Band band1 = bandService.readByName(band.getName());
        return ResponseEntity.ok(bandService.create(band1));
    }

    @GetMapping
    public ResponseEntity<Band> getBand(@RequestParam("bandName") String name) {
        return ResponseEntity.ok(bandService.readByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Band> findBandById(@PathVariable("id") Long id) {
        Band band = bandService.readById(id);
        return ResponseEntity.ok(band);
    }

    @DeleteMapping("/{id}")
    public void deleteBand(@PathVariable("id") Long id) {
        bandService.delete(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Band> updateBand(@PathVariable("id") Long id, @RequestBody Band band) {
        return ResponseEntity.ok(bandService.update(id, band));
    }
}
