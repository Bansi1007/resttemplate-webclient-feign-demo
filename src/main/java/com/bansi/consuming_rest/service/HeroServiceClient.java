package com.bansi.consuming_rest.service;

import com.bansi.consuming_rest.auth.SuperheroTokenProvider;
import com.bansi.consuming_rest.model.Hero;
import com.bansi.consuming_rest.model.HeroRequest;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.*;

@Service
public class HeroServiceClient {
    @Value("${superhero.api.base-url}")
    private String baseUrl;

    @Autowired
    private SuperheroTokenProvider tokenProvider;



    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final RetryRegistry retryRegistry;
    private int attempt = 0;


    public HeroServiceClient(RestTemplate restTemplate, WebClient webClient, RetryRegistry retryRegistry) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
        this.retryRegistry = retryRegistry;
    }

    //1
    public void createHero(HeroRequest request) {
        String url = baseUrl + "/heroes";
        restTemplate.postForObject(url, request, HeroRequest.class);
    }


    @Retry(name = "HeroServiceClient" , fallbackMethod = "fallback")
    public List<Hero> getActiveHeroes() {
        attempt++;
        Instant now = Instant.now();
        System.out.println("attempt "+attempt+ "----"+now );
        String url = baseUrl + "/heroes/active";
            List<Hero> heroes = restTemplate.getForObject(url, List.class);
        attempt = 0;
            return heroes;
    }

    public List<Hero> fallback(Throwable ex) {
        System.out.println("3 retries failed. Sending service unavailable " +
                "to consumer. Please don't send any more requests");
       System.out.println( ex.getMessage());
        attempt = 0;
        return Collections.emptyList();
    }


   // For loop
//    public List<Hero> getActiveHeroes() {
//        for (int i = 1; i <= 3; i++) {
//            attempt++;
//            try {
//                String url = baseUrl + "/heroes/active";
//                List<Hero> activeHeroList = restTemplate.getForObject(url, List.class);
//                if (activeHeroList != null) {
//                    return activeHeroList;
//                }
//                return Collections.emptyList();
//            } catch (Exception e) {
//                System.out.println("Retry " + attempt + ": failed");
//
//                //https://stackoverflow.com/questions/9587673/thread-sleep-vs-timeunit-seconds-sleep
//                if (attempt <= 3) {
//                    try {
//                        TimeUnit.SECONDS.sleep(10);
//                    } catch (InterruptedException ie) {
//                        System.out.println(ie.getMessage());
//                    }
//                }
//            }
//        }
//
//        System.out.println("3 retries failed. Sending service unavailable to consumer. Please dont send any more requests");
//
//        return Collections.emptyList();
//    }


    // 2
//    public List<Hero> getActiveHeroes() {
//        String url = baseUrl + "/heroes/active";
//            List<Hero> heroes = restTemplate.getForObject(url, List.class);
//            return heroes;
//    }


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


   // @RateLimiter(name = "HeroServiceClient",fallbackMethod = "rateLimitFallback")
   public List<Hero> getBenchedHeroes() {
       try {
           return callSuperhero();
       } catch (HttpClientErrorException  e) {
           tokenProvider.forceRefresh();
           return callSuperhero();
       }
   }

    private List<Hero> callSuperhero() {
        String url = baseUrl + "/heroes/benched";
        System.out.println("RestTemplate interceptors attached: " + restTemplate.getInterceptors().size());

        Hero[] heroes = restTemplate.getForObject(url, Hero[].class);
        return Arrays.asList(heroes);
    }

    public Hero toggleHero(UUID id) {
        String url = baseUrl + "/heroes/" + id + "/toggle";
        Hero hero = restTemplate.patchForObject(url, "", Hero.class);
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
