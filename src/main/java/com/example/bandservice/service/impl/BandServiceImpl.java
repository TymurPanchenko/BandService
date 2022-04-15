package com.example.bandservice.service.impl;

import com.example.bandservice.configuration.BandClientProperties;
import com.example.bandservice.controller.BandController;
import com.example.bandservice.exception.NullBandReferenceException;
import com.example.bandservice.model.*;
import com.example.bandservice.repository.BandRepository;
import com.example.bandservice.service.BandService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
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
    public Band create(BandDTO band) {
        Band band1 = bandRepository.findByName(band.getName());
        if (band1 != null) {
            logger.error("The band is in DB");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        List<Band> list = bandRepository.findAllBands();
        Long l;
        try {
            l = list.stream().max((o1, o2) -> (int) (o1.getId() - o2.getId())).get().getId();
        } catch (NoSuchElementException e) {
            l = 0L;
        }
        Band newBand = new Band();
        newBand.setId(++l);
        newBand.setName(band.getName());
        return bandRepository.save(newBand);
    }

    @Override
    public Band readById(Long id) {
        try {
            Band band = bandRepository.getBandById(id);
            band.getName();
            return band;
        } catch (NullPointerException e) {
            logger.error("Band is not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Band is not found");
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
            List<Weapon> listWeapon = weapons.get("weapons").stream().filter(o -> o.getBandId() != null).filter(o -> o.getBandId().equals(id)).collect(Collectors.toList());
            List<Task> listTask = tasks.stream().filter(o -> o.getBandId() != null).filter(o -> o.getBandId().equals(id)).collect(Collectors.toList());
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
    public Boolean getReadyCheck(Long id, HttpServletRequest request) {
        try {
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
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            Long l = listTask.get(0).getId();
            List<User> listUser = users.stream().filter(o -> o.getTaskId() != null).filter(o -> o.getTaskId().equals(l)).collect(Collectors.toList());
            List<Weapon> listWeapon = weapons.get("weapons").stream().filter(o -> o.getTaskId() != null).filter(o -> o.getTaskId().equals(l)).collect(Collectors.toList());
            int x = listUser.size();
            if (x < 1) {
                return false;
            }
            for (Weapon w : listWeapon) {
                x += w.getDamage();
            }
            return x >= listTask.get(0).getStrength();
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getRawStatusCode()));
        }
    }

    @Override
    public Object readByName(String name) {
        Band band = bandRepository.findByName(name);
        if (band == null) {
            logger.info("Band is not found");
            return Collections.emptyList();
        }
        return band;
    }

    @Override
    public Band update(Long id, BandDTO band) {
        Band band1 = readById(id);
        band1.setId(id);
        if (band.getName() != null) {
            band1.setName(band.getName());
        }
        bandRepository.update(id, band1.getName());
        return band1;
    }

    @Override
    public boolean isTokenValidBoss(HttpServletRequest request) {
        try {
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
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public boolean isTokenValidBossAndUser(HttpServletRequest request) {
        try {
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
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private HttpHeaders createHeaders(String jwt) {
        return new HttpHeaders() {{
            set("Authorization", jwt);
        }};
    }

}
