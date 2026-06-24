package com.bansi.consuming_rest.service;

import com.bansi.consuming_rest.model.Hero;
import com.bansi.consuming_rest.model.HeroRequest;
import com.bansi.feign.client.HeroClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class HeroServiceClient {
    @Value("${superhero.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final WebClient webClient;


    public HeroServiceClient(RestTemplate restTemplate, WebClient webClient) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
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
    public Hero updateHero(UUID id, HeroRequest heroRequest) {
        String url = baseUrl + "/heroes/" + id;
        Hero hero = new Hero(heroRequest);
        ResponseEntity<Hero> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(hero),
                Hero.class
        );
        return response.getBody();
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
    public Hero toggleHero(UUID id) {
        String url = baseUrl + "/heroes/" + id+"/toggle";
        Hero hero = restTemplate.patchForObject(url,"", Hero.class);
        return hero;
    }

    //web client
    public Mono<String> getHeroesCount() {
        return webClient.get()
                .uri("/heroes/count")
                .retrieve()
                .bodyToMono(String.class);
    }


    public double getAverageLevelHeroes() {
        return webClient.get()
                .uri("heroes/average-level")
                .retrieve()
                .bodyToMono(Double.class)
                .block();
    }

    public Mono<List<Hero>> bulkHeroes(@RequestBody List<HeroRequest> heroes) {
        return webClient.post()
                .uri("heroes/bulk")
                .bodyValue(heroes)
                .retrieve()
                .bodyToFlux(Hero.class)
                .collectList();
    }

    public Mono<List<String>> getHeroesNames() {
        return webClient.get()
                .uri("heroes/names")
                .retrieve()
                .bodyToMono(String[].class)
                .map(Arrays::asList);
    }


    public Mono<Hero> updateName(UUID id, String name) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("heroes/{id}/rename")
                        .queryParam("name", name)
                        .build(id)) // Binds 'id' to the path variable
                .retrieve()
                .bodyToMono(Hero.class);

    }
}
