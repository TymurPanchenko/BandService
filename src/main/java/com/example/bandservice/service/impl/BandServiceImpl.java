package com.example.bandservice.service.impl;

import com.example.bandservice.configuration.BandClientProperties;
import com.example.bandservice.exception.NullBandReferenceException;
import com.example.bandservice.model.Band;
import com.example.bandservice.model.Task;
import com.example.bandservice.model.User;
import com.example.bandservice.model.Weapon;
import com.example.bandservice.repository.BandRepository;
import com.example.bandservice.service.BandService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BandServiceImpl implements BandService {

    private final BandRepository bandRepository;
    private final RestTemplate restTemplate;
    private final BandClientProperties bandClientProperties;

    public BandServiceImpl(BandRepository bandRepository, HttpComponentsClientHttpRequestFactory factory, BandClientProperties bandClientProperties) {
        this.bandRepository = bandRepository;
        this.restTemplate = new RestTemplate(factory);
        this.bandClientProperties = bandClientProperties;
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
    public Map<String, List<String>> getReport() {
        List<Band> bands = restTemplate.exchange(bandClientProperties.getUrlBands() + "/all",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Band>>() {
                }).getBody();
        Map<String, List<String>> map = new HashMap<>();
        if (bands == null) {
            throw new NullBandReferenceException("Null band reference");
        }
        for (Band b : bands) {
            map.put(b.getName(), getSingleReport(b.getId()));
        }
        return map;
    }


    @Override
    public List<String> getSingleReport(Long id) {
        List<User> users = restTemplate.exchange(bandClientProperties.getUrlUsers(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
                }).getBody();
        Map<String, List<Weapon>> weapons =
                restTemplate.exchange(bandClientProperties.getUrlWeapons(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, List<Weapon>>>() {
                        }).getBody();
        List<Task> tasks =
                restTemplate.exchange(bandClientProperties.getUrlTasks(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Task>>() {
                        }).getBody();
        List<User> listUser = users.stream().filter(o -> o.getBandId().equals(id)).collect(Collectors.toList());
        List<Weapon> listWeapon = weapons.get("weapons").stream().filter(o -> o.getBand_id().equals(id)).collect(Collectors.toList());
        List<Task> listTask = tasks.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
        List<String> s = new ArrayList<>();
        s.add(listUser.toString());
        s.add(listWeapon.toString());
        s.add(listTask.toString());
        return s;
    }

    @Override
    public String getReadyCheck(Long id, Long taskId) {
        if (taskId.equals(0L)) {
            return "Task is already done";
        }
        List<User> users = restTemplate.exchange(bandClientProperties.getUrlUsers(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
                }).getBody();
        Map<String, List<Weapon>> weapons =
                restTemplate.exchange(bandClientProperties.getUrlWeapons(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, List<Weapon>>>() {
                        }).getBody();
        List<Task> tasks =
                restTemplate.exchange(bandClientProperties.getUrlTasks(),
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Task>>() {
                        }).getBody();
        List<Task> listTask = tasks.stream().filter(o -> o.getId().equals(taskId)).collect(Collectors.toList());
        Long l = listTask.get(0).getId();
        List<User> listUser = users.stream().filter(o -> o.getTaskId().equals(l)).collect(Collectors.toList());
        List<Weapon> listWeapon = weapons.get("weapons").stream().filter(o -> o.getTask_id().equals(l)).collect(Collectors.toList());
        int x = listUser.size();
        for (Weapon w : listWeapon) {
            x += w.getDamage();
        }
        return x >= listTask.get(0).getStrength() ? "All is in readiness. Start executing" : "You are not strong enough for this task";
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