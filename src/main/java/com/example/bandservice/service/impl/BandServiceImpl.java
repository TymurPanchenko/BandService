package com.example.bandservice.service.impl;

import com.example.bandservice.exception.NullBandReferenceException;
import com.example.bandservice.model.Band;
import com.example.bandservice.repository.BandRepository;
import com.example.bandservice.service.BandService;
import org.springframework.stereotype.Service;

import javax.lang.model.UnknownEntityException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BandServiceImpl implements BandService {

    private BandRepository bandRepository;

    public BandServiceImpl(BandRepository bandRepository) {
        this.bandRepository = bandRepository;
    }
    @Override
    public Band create(Band band) {
        try {
            return bandRepository.save(band);
        } catch (IllegalArgumentException e) {
            throw new NullBandReferenceException("Band cannot be null");
        }

    }

    @Override
    public Band readById(Long id) {
        Optional<Band> optional = bandRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new NoSuchElementException("Band with id " + id + " not found");
    }

    @Override
    public Band readByName(String name) {
        return bandRepository.findByName(name);
    }

    @Override
    public Band update(Long id, Band band) {
        if (band != null) {
            Band oldBand = readById(id);
            if (band.getName() != null) {
                return bandRepository.update(id, band.getName());
            }
        }
        throw new NullBandReferenceException("User cannot be 'null'");
    }

    @Override
    public void delete(Long id) {
        Band band = readById(id);
        if (band != null) {
            bandRepository.deleteById(id);
        } else {
            throw new NoSuchElementException("Band with id " + id + " not found");
        }
    }

    @Override
    public List<Band> getAll() {
        List<Band> bands = bandRepository.findAllBands();
        return bands.isEmpty() ? new ArrayList<>() : bands;
    }
}
