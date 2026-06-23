package com.bansi.consuming_rest.service;

import com.bansi.consuming_rest.model.Hero;
import com.bansi.consuming_rest.model.HeroRequest;
import com.bansi.feign.client.HeroClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class HeroServiceClient {
    private final String apiUrl = "http://localhost:8080";

    private final RestTemplate restTemplate;

    @Value(apiUrl)
    private String baseUrl;

    public HeroServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    //1
    public void createHero(HeroRequest request) {
        String url = baseUrl + "/heroes";
         restTemplate.postForObject(url, request, HeroRequest.class);
    }

    //2
    public List<Hero> getActiveHeroes(){
        String url = baseUrl + "/heroes/active";
        List<Hero> heroes = restTemplate.getForObject(url, List.class);
        return heroes;
    }

    //3
    public void updateHero(UUID id, HeroRequest heroRequest) {
        String url = baseUrl + "/heroes/" + id;
        Hero hero = new Hero(heroRequest);
        hero.setPower(heroRequest.getPower());
        hero.setLevel(heroRequest.getLevel());
        restTemplate.put(url, hero);
    }

    public void deleteHeroes() {
        String url = baseUrl + "/heroes";
        restTemplate.delete(url);
    }

    public List<Hero> getBenchedHeroes() {
        String url = baseUrl + "/heroes/benched";
        List<Hero> heroes = restTemplate.getForObject(url, List.class);
        return heroes;
    }
//    public Hero toggleHero(UUID id) {
//        String url = baseUrl + "/heroes/" + id+"/toggle";
//        Hero hero = restTemplate.patchForObject(url,"", Hero.class);
//        return hero;
//    }


}
