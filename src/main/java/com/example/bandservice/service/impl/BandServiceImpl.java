package com.example.bandservice.service.impl;

import com.example.bandservice.configuration.BandClientProperties;
import com.example.bandservice.controller.BandController;
import com.example.bandservice.exception.NullBandReferenceException;
import com.example.bandservice.model.Band;
import com.example.bandservice.model.Task;
import com.example.bandservice.model.User;
import com.example.bandservice.model.Weapon;
import com.example.bandservice.repository.BandRepository;
import com.example.bandservice.service.BandService;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;


import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BandServiceImpl implements BandService {

    private final BandRepository bandRepository;
    private final RestTemplate restTemplate;
    private final BandClientProperties bandClientProperties;
    @Value("${my.app.secret}")
    private String jwtSecret;
    private final Logger logger = LoggerFactory.getLogger(BandController.class);

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
        try {
            Band band = bandRepository.getBandById(id);
            band.getName();
            return band;
        } catch (NullPointerException e) {
            logger.error("Band is not found");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void delete(Long id) {
        Band band = readById(id);
        bandRepository.deleteById(id);
    }

    @Override
    public List<Band> getAll() {
        return bandRepository.findAllBands();
    }

    @Override
    public Map<String, List<String>> getReport(HttpServletRequest request) {
        List<Band> bands = restTemplate.exchange(bandClientProperties.getUrlBands(),
                HttpMethod.GET, new HttpEntity<>(createHeaders(request.getHeader("Authorization"))), new ParameterizedTypeReference<List<Band>>() {
                }).getBody();
        Map<String, List<String>> map = new HashMap<>();
        if (bands == null) {
            throw new NullBandReferenceException("There are no bands");
        }
        for (Band b : bands) {
            map.put(b.getName(), getSingleReport(b.getId(), request));
        }
        return map;
    }


    @Override
    public List<String> getSingleReport(Long id, HttpServletRequest request) {
        try {
            Band band = readById(id);
            HttpHeaders headers = createHeaders(request.getHeader("Authorization"));
            List<User> users = restTemplate.exchange(bandClientProperties.getUrlUsers(),
                    HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<User>>() {
                    }).getBody();
            Map<String, List<Weapon>> weapons =
                    restTemplate.exchange(bandClientProperties.getUrlWeapons(),
                            HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<Map<String, List<Weapon>>>() {
                            }).getBody();
            List<Task> tasks =
                    restTemplate.exchange(bandClientProperties.getUrlTasks(),
                            HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<Task>>() {
                            }).getBody();
            List<User> listUser = users.stream().filter(o -> o.getBandId() != null).filter(o -> o.getBandId().equals(id)).collect(Collectors.toList());
            List<Weapon> listWeapon = weapons.get("weapons").stream().filter(o -> o.getTask_id() != null).filter(o -> o.getBand_id().equals(id)).collect(Collectors.toList());
            List<Task> listTask = tasks.stream().filter(o -> o.getId().equals(id)).collect(Collectors.toList());
            List<String> s = new ArrayList<>();
            if (listUser.isEmpty()) {
                s.add("There is no users");
            } else {
                s.add(listUser.toString());
            }
            if (listWeapon.isEmpty()) {
                s.add("There is no weapons");
            } else {
                s.add(listWeapon.toString());
            }
            if (listTask.isEmpty()) {
                s.add("There is no tasks");
            } else {
                s.add(listTask.toString());
            }
            return s;
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getRawStatusCode()));
        }
    }

    @Override
    public String getReadyCheck(Long id, HttpServletRequest request) {
        try {
            if (id.equals(0L)) {
                return "Task is already done";
            }
            HttpHeaders headers = createHeaders(request.getHeader("Authorization"));
            List<User> users = restTemplate.exchange(bandClientProperties.getUrlUsers(),
                    HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<User>>() {
                    }).getBody();
            Map<String, List<Weapon>> weapons =
                    restTemplate.exchange(bandClientProperties.getUrlWeapons(),
                            HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<Map<String, List<Weapon>>>() {
                            }).getBody();
            List<Task> tasks =
                    restTemplate.exchange(bandClientProperties.getUrlTasks(),
                            HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<Task>>() {
                            }).getBody();
            List<Task> listTask = tasks.stream().filter(o -> o.getId() != null).filter(o -> o.getId().equals(id)).collect(Collectors.toList());
            if (listTask.isEmpty()) {
                logger.error("There is no task with id " + id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            Long l = listTask.get(0).getId();
            List<User> listUser = users.stream().filter(o -> o.getTaskId() != null).filter(o -> o.getTaskId().equals(l)).collect(Collectors.toList());
            List<Weapon> listWeapon = weapons.get("weapons").stream().filter(o -> o.getTask_id() != null).filter(o -> o.getTask_id().equals(l)).collect(Collectors.toList());
            int x = listUser.size();
            for (Weapon w : listWeapon) {
                x += w.getDamage();
            }
            return x >= listTask.get(0).getStrength() ? "All is in readiness. Start executing" : "You are not strong enough for this task";
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getRawStatusCode()));
        }
    }

    @Override
    public Band readByName(String name) {
        try {
            Band band = bandRepository.findByName(name);
            band.getName();
            return band;
        } catch (NullPointerException e) {
            logger.error("Band is not found");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Band update(Long id, Band band) {
        try {
            Band band1 = readById(id);
            band1.setId(id);
            if (band.getName() != null) {
                band1.setName(band.getName());
            }
            bandRepository.update(id, band1.getName());
            return band1;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public boolean isTokenValidBoss(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String[] s = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(headerAuth.substring(7)).getBody().getSubject().split(" ");
            if (s[2].contains("ROLE_BOSS")) {
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public boolean isTokenValidBossAndUser(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String[] s = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(headerAuth.substring(7)).getBody().getSubject().split(" ");
            if (s[2].contains("ROLE_BOSS") || s[2].contains("ROLE_USER")) {
                return true;
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private HttpHeaders createHeaders(String jwt) {
        return new HttpHeaders() {{
            set("Authorization", jwt);
        }};
    }

}
