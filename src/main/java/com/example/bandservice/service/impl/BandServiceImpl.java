package com.example.bandservice.service.impl;

import com.example.bandservice.model.Band;
import com.example.bandservice.repository.BandRepository;
import com.example.bandservice.service.BandService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BandServiceImpl implements BandService {

    private final BandRepository bandRepository;

    public BandServiceImpl(BandRepository bandRepository) {
        this.bandRepository = bandRepository;
    }

    @Override
    public Band create(Band band) {
        List<Band> list = getAll();
        Long a;
        try {
            a = list.stream().max((o1, o2) -> {
                return (int) (o1.getId() - o2.getId());
            }).get().getId();
        } catch (NoSuchElementException e) {
            a = 0L;
        }
        band.setId(++a);
        return bandRepository.save(band);
    }

    @Override
    public Band readById(Long id) {
        return bandRepository.getBandById(id);
    }

    @Override
    public void delete(Long id) {
        bandRepository.deleteById(id);
    }

    @Override
    public List<Band> getAll() {
        return bandRepository.findAllBands();
    }

    @Override
    public Band readByName(String name) {
        return bandRepository.findByName(name);
    }

    @Override
    public Band update(Long id, Band band) {
        Band band1 = readById(id);
        band1.setId(id);
        if (band.getName() != null) {
            band1.setName(band.getName());
        }
        bandRepository.update(id, band1.getName());
        return band1;
    }
}
