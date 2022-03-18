package com.example.bandservice.service.impl;

import com.example.bandservice.configuration.BandClientProperties;
import com.example.bandservice.model.Band;
import com.example.bandservice.repository.BandRepository;
import com.example.bandservice.service.BandService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.ResponseEntity.ok;

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

    @GetMapping("/bb/{id}")
    public ResponseEntity<String> getUnicornByIdByEntity(@PathVariable final String id) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(
                "https://mafias-user-service-app.herokuapp.com/api/users/" + id,
                String.class);
    }
    @GetMapping("/report")
    public ResponseEntity<String> updateUsersBand(){
        RestTemplate restTemplate = new RestTemplate();
        //String baseUrl = "https://mafias-user-service-app.herokuapp.com/api/users";
        return restTemplate.getForEntity(
                "https://mafias-user-service-app.herokuapp.com/api/users",
                String.class);
        //ResponseEntity<String> response = null;
        //response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, String.class);
        //logger.info("Updating the User with id: " +id);
//        try {
//            String jsonStr = new String((response.getBody()).getBytes());
//            JSONObject jsonObject = new JSONObject(jsonStr);
//            Long bandId =  Long.valueOf(jsonObject.getString("id"));
//            return ok(userService.updateBandId(id, bandId));
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
    }
}
